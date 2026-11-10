package com.ecommerce.orderservice.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_execution",uniqueConstraints =
@UniqueConstraint(name = "uk_saga_order_id",
        columnNames = "order_id"))
public class SagaExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id",nullable = false,unique = true)
    private Long orderId;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false,length = 30)
    private SagaStep currentStep;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false,length = 20)
    private SagaStatus status;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "recovery_lock_token")
    private String recoveryLockToken;

    @Column(name = "recovery_locked_until")
    private LocalDateTime recoveryLockedUntil;

    public SagaExecution() {
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getRecoveryLockToken() {
        return recoveryLockToken;
    }

    public void setRecoveryLockToken(String recoveryLockToken) {
        this.recoveryLockToken = recoveryLockToken;
    }

    public LocalDateTime getRecoveryLockedUntil() {
        return recoveryLockedUntil;
    }

    public void setRecoveryLockedUntil(LocalDateTime recoveryLockedUntil) {
        this.recoveryLockedUntil = recoveryLockedUntil;
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

    public SagaStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(SagaStep currentStep) {
        this.currentStep = currentStep;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
