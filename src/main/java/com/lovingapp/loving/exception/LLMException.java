package com.lovingapp.loving.exception;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.errors.BadRequestException;
import com.openai.errors.InternalServerException;
import com.openai.errors.OpenAIInvalidDataException;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.PermissionDeniedException;
import com.openai.errors.RateLimitException;
import com.openai.errors.SseException;
import com.openai.errors.UnauthorizedException;
import com.openai.errors.UnprocessableEntityException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LLMException extends RuntimeException {

    private final String openAiMessage;
    private final Type type;

    public enum Type {
        REQUEST_PARSING,
        RESPONSE_PARSING,
        UNAUTHORIZED,
        RATE_LIMIT,
        SERVICE_UNAVAILABLE,
        UNKNOWN
    }

    public LLMException(Throwable cause) {
        super(cause);
        this.type = classify(cause);
        this.openAiMessage = extractOpenAiMessage(cause);
    }

    public Type getType() {
        return type;
    }

    public String getOpenAiMessage() {
        return openAiMessage;
    }

    private static String extractOpenAiMessage(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur.getMessage() != null && !cur.getMessage().isBlank()) {
                return cur.getMessage().trim();
            }
            cur = cur.getCause();
        }
        return "LLM request failed";
    }

    private static Type classify(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof UnauthorizedException || cur instanceof PermissionDeniedException) {
                return Type.UNAUTHORIZED;
            }

            if (cur instanceof RateLimitException) {
                return Type.RATE_LIMIT;
            }

            if (cur instanceof BadRequestException || cur instanceof UnprocessableEntityException
                    || cur instanceof IllegalArgumentException) {
                return Type.REQUEST_PARSING;
            }

            if (cur instanceof OpenAIInvalidDataException || cur instanceof JsonProcessingException) {
                return Type.RESPONSE_PARSING;
            }

            if (cur instanceof OpenAIIoException || cur instanceof IOException || cur instanceof SseException
                    || cur instanceof InternalServerException) {
                return Type.SERVICE_UNAVAILABLE;
            }

            cur = cur.getCause();
        }

        return Type.UNKNOWN;
    }
}
