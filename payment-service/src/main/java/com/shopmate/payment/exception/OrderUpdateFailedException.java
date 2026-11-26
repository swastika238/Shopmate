package com.shopmate.payment.exception;

public class OrderUpdateFailedException extends RuntimeException {
    public OrderUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
