package com.ecommerce.orderservice.dto;

public class InventoryReservationResponse {
    private Long id;
    private Long orderId;
    private Integer quantity;
    private String status;

    public InventoryReservationResponse(Long id, Long orderId, Integer quantity, String status) {
        this.id = id;
        this.orderId = orderId;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
