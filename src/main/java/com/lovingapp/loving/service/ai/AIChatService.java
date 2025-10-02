package com.lovingapp.loving.service.ai;

import com.lovingapp.loving.dto.UserContextDTO;
import com.lovingapp.loving.dto.ai.ChatDTOs;
import com.lovingapp.loving.model.ai.ChatMessage;
import com.lovingapp.loving.model.ai.ChatMessageRole;
import com.lovingapp.loving.model.ai.ChatSession;
import com.lovingapp.loving.model.ai.ChatSessionStatus;
import com.lovingapp.loving.model.enums.*;
import com.lovingapp.loving.repository.ai.ChatMessageRepository;
import com.lovingapp.loving.repository.ai.ChatSessionRepository;
import com.lovingapp.loving.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final OpenAiChatClient openAiChatClient;
    private final UserContextService userContextService;

    private static final String SYSTEM_PROMPT = "You are Loving AI Companion. Talk empathetically, ask concise follow-up questions to understand: emotional states, relationship needs, love languages, tones, time/effort/intensity, life contexts, relationship status, and summarize a semantic intent. When ready for recommendations, signal READY. Otherwise, ask exactly one focused follow-up question. Output plain text for the user. After your reply, include a machine section between <context_json>...</context_json> containing a JSON object with keys: emotionalStates[], relationalNeeds[], preferredLoveLanguages[], preferredRitualTypes[], preferredTones[], availableTimeMinutes, preferredEffortLevel, preferredIntensity, currentContexts[], timeContext, relationshipStatus, semanticQuery, and a flag readyForRecommendation: true|false.";

    @Transactional
    public ChatDTOs.StartSessionResponse startSession(ChatDTOs.StartSessionRequest req) {
        ChatSession session = ChatSession.builder()
                .userId(req.getUserId())
                .conversationId(req.getConversationId())
                .status(ChatSessionStatus.ACTIVE)
                .build();
        session = sessionRepository.save(session);

        // Optional: seed a system prompt message
        if (req.getSystemPrompt() != null && !req.getSystemPrompt().isBlank()) {
            messageRepository.save(ChatMessage.builder()
                    .sessionId(session.getId())
                    .role(ChatMessageRole.SYSTEM)
                    .content(req.getSystemPrompt())
                    .build());
        } else {
            messageRepository.save(ChatMessage.builder()
                    .sessionId(session.getId())
                    .role(ChatMessageRole.SYSTEM)
                    .content(SYSTEM_PROMPT)
                    .build());
        }

        return ChatDTOs.StartSessionResponse.builder()
                .sessionId(session.getId())
                .userId(session.getUserId())
                .conversationId(session.getConversationId())
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Transactional
    public Mono<ChatDTOs.SendMessageResponse> sendMessage(UUID sessionId, ChatDTOs.SendMessageRequest req) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("ChatSession not found: " + sessionId));

        // Persist user message
        ChatMessage userMsg = messageRepository.save(ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatMessageRole.USER)
                .content(req.getContent())
                .build());

        // Build message history for LLM
        List<OpenAiChatClient.Message> llmMessages = new ArrayList<>();
        // Always include latest system prompt from history or default
        List<ChatMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        if (history.stream().noneMatch(m -> m.getRole() == ChatMessageRole.SYSTEM)) {
            llmMessages.add(OpenAiChatClient.Message.of(ChatMessageRole.SYSTEM, SYSTEM_PROMPT));
        }
        for (ChatMessage m : history) {
            llmMessages.add(OpenAiChatClient.Message.of(m.getRole(), m.getContent()));
        }

        // Call LLM
        return openAiChatClient.chat(llmMessages)
                .map(assistantText -> {
                    // Persist assistant message
                    ChatMessage assistantMsg = messageRepository.save(ChatMessage.builder()
                            .sessionId(session.getId())
                            .role(ChatMessageRole.ASSISTANT)
                            .content(assistantText)
                            .build());

                    session.setUpdatedAt(OffsetDateTime.now());
                    sessionRepository.save(session);

                    // Extract context JSON and update user context
                    ExtractionResult extraction = extractContext(assistantText);
                    boolean recommendationTriggered = false;
                    if (extraction.contextDTO != null) {
                        // attach userId and conversationId
                        extraction.contextDTO.setUserId(session.getUserId());
                        extraction.contextDTO.setConversationId(session.getConversationId());

                        // upsert: get latest and update, else create
                        Optional<UserContextDTO> latest = userContextService.getLatestUserContext(session.getUserId());
                        if (latest.isPresent()) {
                            userContextService.updateUserContext(latest.get().getId(), extraction.contextDTO);
                        } else {
                            userContextService.createUserContext(extraction.contextDTO);
                        }
                    }

                    if (extraction.readyForRecommendation) {
                        recommendationTriggered = triggerRecommendation(extraction.contextDTO);
                        if (recommendationTriggered) {
                            session.setStatus(ChatSessionStatus.COMPLETED);
                            sessionRepository.save(session);
                        }
                    }

                    return ChatDTOs.SendMessageResponse.builder()
                            .userMessage(toDto(userMsg))
                            .assistantMessage(toDto(assistantMsg))
                            .askedFollowUp(!extraction.readyForRecommendation)
                            .recommendationTriggered(recommendationTriggered)
                            .build();
                });
    }

    @Transactional(readOnly = true)
    public ChatDTOs.HistoryResponse getHistory(UUID sessionId) {
        List<ChatMessage> msgs = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return ChatDTOs.HistoryResponse.builder()
                .sessionId(sessionId)
                .messages(msgs.stream().map(this::toDto).collect(Collectors.toList()))
                .build();
    }

    private ChatDTOs.ChatMessageDTO toDto(ChatMessage m) {
        return ChatDTOs.ChatMessageDTO.builder()
                .id(m.getId())
                .sessionId(m.getSessionId())
                .role(m.getRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }

    // === Context extraction ===
    private static final Pattern CONTEXT_JSON_PATTERN = Pattern.compile(
            "<context_json>\\s*(?:\\n|\\r\\n)?(\\{[\\s\\S]*?\\})\\s*(?:\\n|\\r\\n)?</context_json>",
            Pattern.CASE_INSENSITIVE
    );

    private ExtractionResult extractContext(String assistantText) {
        try {
            Matcher matcher = CONTEXT_JSON_PATTERN.matcher(assistantText);
            if (matcher.find()) {
                String json = matcher.group(1);
                Map<String, Object> map = parseJson(json);
                UserContextDTO dto = mapToUserContextDTO(map);
                boolean ready = getBoolean(map.get("readyForRecommendation"));
                return new ExtractionResult(dto, ready);
            }
        } catch (Exception ignored) {}
        return new ExtractionResult(null, false);
    }

    private UserContextDTO mapToUserContextDTO(Map<String, Object> map) {
        UserContextDTO dto = new UserContextDTO();
        dto.setEmotionalStates(toEnumList(map.get("emotionalStates"), EmotionalState.class));
        dto.setRelationalNeeds(toEnumList(map.get("relationalNeeds"), RelationalNeed.class));
        dto.setPreferredLoveLanguages(toEnumList(map.get("preferredLoveLanguages"), LoveType.class));
        dto.setPreferredRitualTypes(toEnumList(map.get("preferredRitualTypes"), RitualType.class));
        dto.setPreferredTones(toEnumList(map.get("preferredTones"), RitualTone.class));
        dto.setAvailableTimeMinutes(getInteger(map.get("availableTimeMinutes")));
        dto.setPreferredEffortLevel(toEnum(map.get("preferredEffortLevel"), EffortLevel.class));
        dto.setPreferredIntensity(toEnum(map.get("preferredIntensity"), IntensityLevel.class));
        dto.setCurrentContexts(toEnumList(map.get("currentContexts"), LifeContext.class));
        dto.setTimeContext(toEnum(map.get("timeContext"), TimeContext.class));
        dto.setRelationshipStatus(toEnum(map.get("relationshipStatus"), RelationshipStatus.class));
        dto.setSemanticQuery(asString(map.get("semanticQuery")));
        dto.setLastInteractionAt(OffsetDateTime.now());
        return dto;
    }

    private boolean triggerRecommendation(UserContextDTO context) {
        // Delegate to stub service for now
        return recommendationServiceTrigger(context);
    }

    // Stubbed recommendation trigger; replace with actual engine integration later
    private boolean recommendationServiceTrigger(UserContextDTO context) {
        return ritualRecommendationService.triggerRecommendations(context);
    }

    // Injected stub service
    private final RitualRecommendationService ritualRecommendationService;

    // === Helpers ===
    private Map<String, Object> parseJson(String json) {
        // Very small dependency-less parser strategy using Jackson available via Spring
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Integer getInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static boolean getBoolean(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> List<E> toEnumList(Object o, Class<E> enumType) {
        if (o == null) return new ArrayList<>();
        List<Object> raw;
        if (o instanceof List<?> list) raw = (List<Object>) list; else raw = List.of(o);
        List<E> out = new ArrayList<>();
        for (Object item : raw) {
            E e = toEnum(item, enumType);
            if (e != null) out.add(e);
        }
        return out;
    }

    private static <E extends Enum<E>> E toEnum(Object o, Class<E> enumType) {
        if (o == null) return null;
        String s = String.valueOf(o).trim().toUpperCase().replace(' ', '_');
        try { return Enum.valueOf(enumType, s); } catch (IllegalArgumentException e) { return null; }
    }

    private record ExtractionResult(UserContextDTO contextDTO, boolean readyForRecommendation) {}
}
