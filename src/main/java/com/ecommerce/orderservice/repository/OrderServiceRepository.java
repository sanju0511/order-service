package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderServiceRepository extends JpaRepository<Order,Long> {
}
