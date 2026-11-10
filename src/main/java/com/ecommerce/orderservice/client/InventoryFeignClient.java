package com.ecommerce.orderservice.client;


import com.ecommerce.orderservice.dto.InventoryReservationResponse;
import com.ecommerce.orderservice.dto.InventoryResponse;
import com.ecommerce.orderservice.dto.StockOperationRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service",
url = "${inventory.service.url}",
configuration = InventoryFeignConfiguration.class)
public interface InventoryFeignClient {

    @PostMapping(value = "/inventory/{productId}/reserve")
    InventoryResponse reserveStock(@PathVariable Long productId,@RequestParam("order_id") Long orderId,
                                   @RequestBody StockOperationRequest request);

    @PostMapping(value = "/inventory/{productId}/release")
    InventoryResponse releaseStock(@PathVariable Long productId,@RequestParam("order_id") Long orderId,
                                   @RequestBody StockOperationRequest request);

    @PostMapping(value = "/inventory/{productId}/confirm")
    InventoryResponse confirmStock(@PathVariable Long productId,@RequestParam("order_id") Long orderId,
                                   @RequestBody StockOperationRequest request);


    @GetMapping("/inventory/{orderId}/reservations")
    InventoryReservationResponse getReservationByOrderId(
            @PathVariable("orderId") Long orderId
    );
}
