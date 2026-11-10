package com.ecommerce.orderservice.controller;


import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.SagaRecoveryDecision;
import com.ecommerce.orderservice.dto.SagaResponse;
import com.ecommerce.orderservice.entities.OrderStatus;
import com.ecommerce.orderservice.service.serviceImpl.OrderServiceImpl;
import com.ecommerce.orderservice.service.serviceImpl.SagaExecutionService;
import com.ecommerce.orderservice.service.serviceImpl.SagaRecoveryDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {
   private final OrderServiceImpl orderService;
   private final SagaRecoveryDecisionService recoveryDecisionService;



   private final SagaExecutionService executionService;

    public OrderController(OrderServiceImpl orderService, SagaRecoveryDecisionService recoveryDecisionService, SagaExecutionService executionService) {
        this.orderService = orderService;
        this.recoveryDecisionService = recoveryDecisionService;
        this.executionService = executionService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request){
        return new ResponseEntity<>(orderService.createOrder(request,idempotencyKey), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id){
        return  new ResponseEntity<>(orderService.getOrderById(id),HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        return  new ResponseEntity<>(orderService.getAllOrders(),HttpStatus.OK);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id
            ,@Valid @RequestBody CreateOrderRequest request){
        return new ResponseEntity<>(orderService.updateOrder(id,request),HttpStatus.OK);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id){
        return new ResponseEntity<>(orderService.cancelOrder(id),HttpStatus.OK);
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status){
        return new ResponseEntity<>(orderService.updateStatus(id,status),HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/saga")
    public ResponseEntity<SagaResponse> getSagaDetails(@PathVariable Long id){
        return  new ResponseEntity<>(executionService.getSagaByOrderId(id),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
    }

    @GetMapping("/{id}/recovery-decision")
    public ResponseEntity<SagaRecoveryDecision> getRecoveryDecision(
            @PathVariable Long id) {

        SagaRecoveryDecision decision =
                recoveryDecisionService.decide(id);

        return ResponseEntity.ok(decision);
    }
}
