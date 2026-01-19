package com.lovingapp.loving.exception;

public class LlmClientException extends RuntimeException {
    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
