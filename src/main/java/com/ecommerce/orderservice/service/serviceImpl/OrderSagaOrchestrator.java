package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.client.InventoryFeignClient;
import com.ecommerce.orderservice.client.PaymentFeignClient;
import com.ecommerce.orderservice.dto.*;
import com.ecommerce.orderservice.entities.Order;
import com.ecommerce.orderservice.entities.OrderStatus;
import com.ecommerce.orderservice.entities.SagaStep;
import com.ecommerce.orderservice.mapper.OrderMapperClass;
import com.ecommerce.orderservice.repository.OrderServiceRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderSagaOrchestrator {
    private final OrderServiceRepository repository;
    private final InventoryFeignClient client;
    private final PaymentFeignClient paymentFeignClient;
    private final SagaExecutionService executionService;

    public OrderSagaOrchestrator(OrderServiceRepository repository, InventoryFeignClient client, PaymentFeignClient paymentFeignClient, SagaExecutionService executionService) {
        this.repository = repository;
        this.client = client;
        this.paymentFeignClient = paymentFeignClient;
        this.executionService = executionService;
    }
    public OrderResponse createOrder(CreateOrderRequest request) {
        StockOperationRequest quantity=new StockOperationRequest(request.getQuantity());

        Order order=new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(request.getUnitPrice());
        order.setTotalAmount(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setOrderStatus(OrderStatus.CREATED);
        Order savedOrder= repository.saveAndFlush(order);

        executionService.startSaga(savedOrder.getId());

        boolean stockReserved=false;
        boolean paymentSucceeded=false;
        boolean compensationSucceeded=true;

        try {

            InventoryResponse rep=client.reserveStock(request.getProductId(), savedOrder.getId(), quantity);

            executionService.updateSaga(savedOrder.getId(), SagaStep.INVENTORY_RESERVED);

            stockReserved=true;
           PaymentResponse payment=paymentFeignClient.paymentProcess(
                   new PaymentRequest(savedOrder.getId()
                   ,savedOrder.getTotalAmount()));

           if(!PaymentStatus.SUCCESS.equals(payment.getStatus())){
               throw new RuntimeException("Payment failed");
           }
           executionService.updateSaga(savedOrder.getId(), SagaStep.PAYMENT_COMPLETED);

            paymentSucceeded=true;
           client.confirmStock(request.getProductId(),savedOrder.getId(), quantity);

            executionService.updateSaga(savedOrder.getId(), SagaStep.INVENTORY_CONFIRMED);
           savedOrder.setOrderStatus(OrderStatus.CONFIRMED);

           Order confirmedOrder=repository.save(savedOrder);
           executionService.completeSaga(savedOrder.getId());
            return OrderMapperClass.toDto(confirmedOrder);

        } catch (RuntimeException e) {

            try {
                executionService.markCompensating(
                        savedOrder.getId(),
                        e.getMessage()
                );
            } catch (RuntimeException sagaError) {
                compensationSucceeded = false;
                e.addSuppressed(sagaError);
            }


            if(paymentSucceeded){
                try {
                    paymentFeignClient.refundPayment(order.getId());
                    executionService.updateSaga(savedOrder.getId(), SagaStep.REFUND_COMPLETED);
                } catch (RuntimeException refund) {
                    compensationSucceeded=false;
                    e.addSuppressed(refund);

                }

            }
            if(stockReserved){
                try {
                    client.releaseStock(request.getProductId(),savedOrder.getId(), quantity);
                    executionService.updateSaga(savedOrder.getId(), SagaStep.INVENTORY_RELEASED);
                } catch (Exception ex) {
                    compensationSucceeded=false;

                    e.addSuppressed(ex);

                }
            }
            if(compensationSucceeded) {
                savedOrder.setOrderStatus(OrderStatus.CANCELLED);
                executionService.markCancelled(order.getId(), "Order Cancelled after compensation");
                repository.save(savedOrder);
            }else {

                try {
                    executionService.markFailed(
                            savedOrder.getId(),
                            "Compensation requires recovery: " + e.getMessage()
                    );
                } catch (RuntimeException sagaError) {
                    e.addSuppressed(sagaError);
                }
            }
            throw e;
        }


    }

}
