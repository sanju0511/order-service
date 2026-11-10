package com.ecommerce.orderservice.client;


import com.ecommerce.orderservice.dto.PaymentRequest;
import com.ecommerce.orderservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service",
url = "${payment.service.url}")
public interface PaymentFeignClient {

    @PostMapping("/payments")
    PaymentResponse paymentProcess(@RequestBody PaymentRequest request);

    @PostMapping("/payments/{orderId}/refund")
    PaymentResponse refundPayment(@PathVariable Long orderId);

    @GetMapping("/payments/{orderId}")
    PaymentResponse getPaymentByOrderId(@PathVariable Long orderId);
}
