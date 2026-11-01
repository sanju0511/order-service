package com.ecommerce.orderservice.dto;

public class StockOperationRequest {
    private Integer quantity;

    public StockOperationRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
