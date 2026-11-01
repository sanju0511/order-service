package com.ecommerce.orderservice.client;


import com.ecommerce.orderservice.dto.InventoryResponse;
import com.ecommerce.orderservice.dto.StockOperationRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service",
url = "${inventory.service.url}",
configuration = InventoryFeignConfiguration.class)
public interface InventoryFeignClient {

    @PostMapping(value = "/inventory/{productId}/reserve")
    InventoryResponse reserveStock(@PathVariable Long productId,
                                   @RequestBody StockOperationRequest request);
}
