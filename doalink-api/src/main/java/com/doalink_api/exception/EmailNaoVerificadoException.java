package com.doalink_api.exception;

public class EmailNaoVerificadoException extends RuntimeException {
    public EmailNaoVerificadoException(String message) {
        super(message);
    }
}
