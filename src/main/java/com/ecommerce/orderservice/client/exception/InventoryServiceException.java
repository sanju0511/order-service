package com.ecommerce.orderservice.client.exception;

import org.springframework.http.HttpStatus;


public class InventoryServiceException  extends RuntimeException {
    private HttpStatus status;

    public InventoryServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
