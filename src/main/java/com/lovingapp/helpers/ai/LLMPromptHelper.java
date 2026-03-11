package com.lovingapp.helpers.ai;

import java.util.List;
import java.util.stream.Collectors;

import com.lovingapp.model.dto.RitualPackDTO;
import com.lovingapp.model.entity.ChatMessage;
import com.lovingapp.model.enums.ChatMessageRole;
import com.lovingapp.model.enums.LoveType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LLMPromptHelper {

    public static String generateConversationVariable(List<ChatMessage> messages) {
        String conversationText = messages.stream()
                .map(m -> {
                    String roleLabel = m.getRole() == ChatMessageRole.USER ? "[User]" : "[Assistant]";
                    return roleLabel + "\n" + m.getContent();
                })
                .collect(Collectors.joining("\n\n"));

        return conversationText;
    }

    public static String generateRitualPackVariable(RitualPackDTO ritualPack) {
        String ritualPackString = new StringBuilder()
                .append("Title: ").append(ritualPack.getTitle()).append("\n")
                .append("Tagline: ").append(ritualPack.getTagLine()).append("\n")
                .append("Description: ").append(ritualPack.getDescription()).append("\n")
                .append("How It Helps: ").append(ritualPack.getHowItHelps()).append("\n")
                .append("Love Types: ")
                .append(String.join(", ", ritualPack.getLoveTypes().stream().map(LoveType::name).toList()))
                .toString();

        return ritualPackString;
    }
}
