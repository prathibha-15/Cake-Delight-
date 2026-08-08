package com.cakedelight.order.exception;

public class BasketItemNotFoundException extends RuntimeException {

    public BasketItemNotFoundException(String message) {
        super(message);
    }
}