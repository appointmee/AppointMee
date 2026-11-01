package com.gg.turnlook.common.exception;

public class EmailNoVerificadoException extends RuntimeException {
    public EmailNoVerificadoException() {
        super("Verificá tu email para iniciar sesión");
    }
}
