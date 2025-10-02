package com.lovingapp.loving.service.ai;

import com.lovingapp.loving.dto.UserContextDTO;
import com.lovingapp.loving.dto.ai.ChatDTOs;
import com.lovingapp.loving.model.ai.ChatMessage;
import com.lovingapp.loving.model.ai.ChatMessageRole;
import com.lovingapp.loving.model.ai.ChatSession;
import com.lovingapp.loving.model.ai.ChatSessionStatus;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.repository.ai.ChatMessageRepository;
import com.lovingapp.loving.repository.ai.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for managing AI chat sessions and messages.
 * Handles the conversation flow with the LLM and context extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AIChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final LlmClient llmClient;
    private final RitualRecommendationService ritualRecommendationService;

    @Override
    @Transactional
    public Mono<ChatDTOs.StartSessionResponse> startSession(ChatDTOs.StartSessionRequest request) {
        UUID userId = request.getUserId();
        String conversationId = request.getConversationId();
        
        // Try to find existing session with this conversation ID
        Mono<ChatSession> sessionMono = conversationId != null 
                ? Mono.fromCallable(() -> sessionRepository.findByUserIdAndConversationId(userId, conversationId)
                        .orElseGet(() -> createNewSession(userId, conversationId)))
                : Mono.fromCallable(() -> createNewSession(userId, null));

        return sessionMono.flatMap(session -> {
            // Get existing messages for this session
            List<ChatMessage> existingMessages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
            
            // If this is a new session or has no messages, add the system prompt
            if (existingMessages.isEmpty()) {
                String systemPrompt = request.getSystemPrompt() != null 
                        ? request.getSystemPrompt() 
                        : "You are a compassionate and empathetic AI companion. Your goal is to help users explore their feelings and relationships in a supportive way. Be warm, understanding, and ask thoughtful questions to better understand their emotional state and needs.";
                
                ChatMessage systemMessage = ChatMessage.builder()
                        .sessionId(session.getId())
                        .role(ChatMessageRole.SYSTEM)
                        .content(systemPrompt)
                        .build();
                
                messageRepository.save(systemMessage);
                existingMessages.add(systemMessage);
            }
            
            return Mono.just(ChatDTOs.StartSessionResponse.builder()
                    .sessionId(session.getId())
                    .conversationId(session.getConversationId())
                    .messages(existingMessages.stream()
                            .map(this::toDto)
                            .collect(Collectors.toList()))
                    .build());
        });
    }

    @Override
    @Transactional
    public Mono<ChatDTOs.SendMessageResponse> sendMessage(UUID sessionId, ChatDTOs.SendMessageRequest request) {
        // 1. Save user message
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessageRole.USER)
                .content(request.getContent())
                .build();
        
        return Mono.fromCallable(() -> messageRepository.save(userMessage))
                .flatMap(savedUserMessage -> {
                    // 2. Get conversation history
                    List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                    
                    // 3. Convert to LLM message format
                    List<LlmClient.Message> llmMessages = messages.stream()
                            .map(m -> new LlmClient.Message(m.getRole().name().toLowerCase(), m.getContent()))
                            .collect(Collectors.toList());
                    
                    // 4. Get AI response
                    return llmClient.chat(llmMessages)
                            .flatMap(assistantResponse -> {
                                // 5. Save assistant's response
                                ChatMessage assistantMessage = ChatMessage.builder()
                                        .sessionId(sessionId)
                                        .role(ChatMessageRole.ASSISTANT)
                                        .content(assistantResponse)
                                        .build();
                                
                                ChatMessage savedAssistantMessage = messageRepository.save(assistantMessage);
                                
                                // 6. Extract context from the conversation
                                ExtractionResult extraction = extractContext(messages, savedUserMessage, savedAssistantMessage);
                                
                                // 7. If we have enough context, trigger recommendations
                                boolean recommendationTriggered = false;
                                if (extraction.readyForRecommendation) {
                                    recommendationTriggered = recommendationServiceTrigger(extraction.contextDTO);
                                    
                                    // Update session status if we're done
                                    sessionRepository.findById(sessionId).ifPresent(session -> {
                                        session.setStatus(ChatSessionStatus.COMPLETED);
                                        sessionRepository.save(session);
                                    });
                                }
                                
                                // 8. Return the response
                                return Mono.just(ChatDTOs.SendMessageResponse.builder()
                                        .assistantMessage(toDto(savedAssistantMessage))
                                        .askedFollowUp(!extraction.readyForRecommendation)
                                        .recommendationTriggered(recommendationTriggered)
                                        .build());
                            });
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ChatDTOs.GetHistoryResponse> getChatHistory(UUID sessionId) {
        return Mono.fromCallable(() -> {
            List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            return ChatDTOs.GetHistoryResponse.builder()
                    .messages(messages.stream()
                            .map(this::toDto)
                            .collect(Collectors.toList()))
                    .build();
        });
    }

    // === Private helper methods ===
    
    private ChatSession createNewSession(UUID userId, String conversationId) {
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .conversationId(conversationId != null ? conversationId : UUID.randomUUID().toString())
                .status(ChatSessionStatus.ACTIVE)
                .build();
        return sessionRepository.save(session);
    }
    
    private ChatDTOs.ChatMessageDTO toDto(ChatMessage message) {
        return ChatDTOs.ChatMessageDTO.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
    
    private ExtractionResult extractContext(List<ChatMessage> messages, ChatMessage userMessage, ChatMessage assistantMessage) {
        // This is a simplified example - in a real app, you'd use more sophisticated NLP techniques
        // to extract meaningful context from the conversation.
        // For now, we'll just extract some basic information.
        
        UserContextDTO context = UserContextDTO.builder().build();
        boolean readyForRecommendation = false;
        
        // Simple keyword matching for demonstration
        String userMessageLower = userMessage.getContent().toLowerCase();
        
        // Extract emotional state
        if (userMessageLower.contains("happy") || userMessageLower.contains("joy")) {
            context.setEmotionalStates(Collections.singletonList(EmotionalState.HAPPY));
        } else if (userMessageLower.contains("sad") || userMessageLower.contains("upset")) {
            context.setEmotionalStates(Collections.singletonList(EmotionalState.SAD));
        } else if (userMessageLower.contains("angry") || userMessageLower.contains("mad")) {
            context.setEmotionalStates(Collections.singletonList(EmotionalState.FRUSTRATED));
        } else if (userMessageLower.contains("anxious") || userMessageLower.contains("worried")) {
            context.setEmotionalStates(Collections.singletonList(EmotionalState.ANXIOUS));
        }
        
        // Extract love language preferences (simplified)
        if (userMessageLower.contains("gift") || userMessageLower.contains("present")) {
            context.setPreferredLoveLanguages(Collections.singletonList(LoveType.FIRE));
        } else if (userMessageLower.contains("time") && userMessageLower.contains("together")) {
            context.setPreferredLoveLanguages(Collections.singletonList(LoveType.BUILD));
        } else if (userMessageLower.contains("touch") || userMessageLower.contains("hug")) {
            context.setPreferredLoveLanguages(Collections.singletonList(LoveType.SPARK));
        } else if (userMessageLower.contains("compliment") || userMessageLower.contains("affirmation")) {
            context.setPreferredLoveLanguages(Collections.singletonList(LoveType.CARE));
        } else if (userMessageLower.contains("help") || userMessageLower.contains("support")) {
            context.setPreferredLoveLanguages(Collections.singletonList(LoveType.CARE));
        }
        
        // If we've extracted some meaningful context, consider the conversation ready for recommendations
        if ((context.getEmotionalStates() != null && !context.getEmotionalStates().isEmpty()) || 
            (context.getPreferredLoveLanguages() != null && !context.getPreferredLoveLanguages().isEmpty())) {
            readyForRecommendation = true;
        }
        
        return new ExtractionResult(context, readyForRecommendation);
    }
    
    private boolean recommendationServiceTrigger(UserContextDTO context) {
        // In a real app, this would call the actual recommendation service
        // For now, we'll just log and return true to indicate success
        log.info("Triggering recommendations for context: {}", context);
        return ritualRecommendationService.triggerRecommendations(context);
    }
    
    
    /**
     * Internal record to hold the result of context extraction.
     */
    private record ExtractionResult(UserContextDTO contextDTO, boolean readyForRecommendation) {}
}
