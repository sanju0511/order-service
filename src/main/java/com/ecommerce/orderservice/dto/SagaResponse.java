package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.entities.SagaStatus;
import com.ecommerce.orderservice.entities.SagaStep;

import java.time.LocalDateTime;

public class SagaResponse {

    private Long orderId;
    private SagaStep currentStep;
    private SagaStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SagaResponse(Long orderId, SagaStep currentStep,
                        SagaStatus status, String failureReason,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.currentStep = currentStep;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getOrderId() { return orderId; }
    public SagaStep getCurrentStep() { return currentStep; }
    public SagaStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}