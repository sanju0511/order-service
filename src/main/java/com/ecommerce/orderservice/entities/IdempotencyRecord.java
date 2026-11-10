package com.ecommerce.orderservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_records",uniqueConstraints =
@UniqueConstraint(name = "uk_idempotency_key",
        columnNames = "idempotency_key"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key",nullable = false,unique = true)
    private String idempotencyKey;

    private Long orderId;

    @Column(nullable = false,length = 64)
    private String requestHash;

    @Column(nullable = false)
    private String status;

    public IdempotencyRecord() {
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
