package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.client.exception.InventoryFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryFeignConfiguration {
    @Bean
    public ErrorDecoder inventoryErrorDecoder() {
        return new InventoryFeignErrorDecoder();
    }

}
