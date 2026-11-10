package com.ecommerce.orderservice.dto;

public enum SagaRecoveryDecision {

    COMPLETE_ORDER,

    CONFIRM_INVENTORY,

    RELEASE_INVENTORY,

    REFUND_PAYMENT,

    MANUAL_INTERVENTION
}
