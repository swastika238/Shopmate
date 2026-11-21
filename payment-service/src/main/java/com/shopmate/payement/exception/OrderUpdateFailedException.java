package com.shopmate.payement.exception;

public class OrderUpdateFailedException extends RuntimeException {
    public OrderUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
