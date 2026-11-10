package com.ecommerce.orderservice.entities;

public enum SagaStep {

    ORDER_CREATED,
    INVENTORY_RESERVED,
    PAYMENT_COMPLETED,
    INVENTORY_CONFIRMED,
    REFUND_COMPLETED,
    INVENTORY_RELEASED,
    COMPLETED
}
