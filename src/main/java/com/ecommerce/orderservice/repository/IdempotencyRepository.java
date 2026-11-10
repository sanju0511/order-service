package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entities.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord,Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String key);
}
