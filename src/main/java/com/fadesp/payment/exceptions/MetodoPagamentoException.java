package com.fadesp.payment.exceptions;

public class MetodoPagamentoException extends RuntimeException {

    public MetodoPagamentoException(String message) {
        super(message);
    }

    public MetodoPagamentoException(String message, Throwable cause) {
        super(message, cause);
    }
}