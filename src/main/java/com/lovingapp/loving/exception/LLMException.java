package com.lovingapp.loving.exception;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.errors.BadRequestException;
import com.openai.errors.InternalServerException;
import com.openai.errors.OpenAIInvalidDataException;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIServiceException;
import com.openai.errors.PermissionDeniedException;
import com.openai.errors.RateLimitException;
import com.openai.errors.SseException;
import com.openai.errors.UnauthorizedException;
import com.openai.errors.UnexpectedStatusCodeException;
import com.openai.errors.UnprocessableEntityException;

public class LLMException extends RuntimeException {

    public enum Type {
        REQUEST_PARSING,
        RESPONSE_PARSING,
        UNAUTHORIZED,
        RATE_LIMIT,
        SERVICE_UNAVAILABLE,
        UNKNOWN
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }

    public static Type classify(Throwable t) {
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

            if (cur instanceof OpenAIServiceException || cur instanceof UnexpectedStatusCodeException) {
                // Best-effort: treat unknown server-side errors as service unavailable for MVP.
                return Type.SERVICE_UNAVAILABLE;
            }

            cur = cur.getCause();
        }

        return Type.UNKNOWN;
    }
}
