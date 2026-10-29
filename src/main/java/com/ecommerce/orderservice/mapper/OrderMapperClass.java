package com.ecommerce.orderservice.mapper;

import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entities.Order;

public class OrderMapperClass {
    public static OrderResponse toDto(Order order){
        return new OrderResponse(order.getId(),order.getProductId()
                ,order.getQuantity(),order.getUnitPrice()
                ,order.getTotalAmount(),order.getOrderStatus());
        
                
    }
    
}
