package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.client.InventoryFeignClient;
import com.ecommerce.orderservice.client.PaymentFeignClient;
import com.ecommerce.orderservice.dto.InventoryReservationResponse;
import com.ecommerce.orderservice.dto.PaymentResponse;
import com.ecommerce.orderservice.dto.SagaRecoveryDecision;
import com.ecommerce.orderservice.entities.SagaExecution;
import com.ecommerce.orderservice.entities.SagaStatus;

import com.ecommerce.orderservice.repository.SagaExecutorRepository;
import org.springframework.stereotype.Service;

@Service
public class SagaRecoveryDecisionService {

    private final SagaExecutorRepository sagaRepository;
    private final InventoryFeignClient inventoryClient;
    private final PaymentFeignClient paymentClient;

    public SagaRecoveryDecisionService(
            SagaExecutorRepository sagaRepository,
            InventoryFeignClient inventoryClient,
            PaymentFeignClient paymentClient) {

        this.sagaRepository = sagaRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public SagaRecoveryDecision decide(Long orderId) {

        try {
            // 1. Retrieve Saga execution
            SagaExecution saga = sagaRepository.findByOrderId(orderId)
                    .orElse(null);

            if (saga == null) {
                return SagaRecoveryDecision.MANUAL_INTERVENTION;
            }

            // 2. Only unfinished Sagas need recovery decisions
            if (saga.getStatus() != SagaStatus.RUNNING
                    && saga.getStatus() != SagaStatus.COMPENSATING) {
                return SagaRecoveryDecision.MANUAL_INTERVENTION;
            }

            // 3. Retrieve Inventory reservation
            InventoryReservationResponse inventory =
                    inventoryClient.getReservationByOrderId(orderId);

            // 4. Retrieve Payment details
            PaymentResponse payment =
                    paymentClient.getPaymentByOrderId(orderId);

            String inventoryStatus = inventory.getStatus();
            String paymentStatus = String.valueOf(payment.getStatus());

            // 5. Decide the recovery action
            if ("SUCCESS".equals(paymentStatus)
                    && "CONFIRMED".equals(inventoryStatus)) {

                return SagaRecoveryDecision.COMPLETE_ORDER;
            }

            if ("SUCCESS".equals(paymentStatus)
                    && "RESERVED".equals(inventoryStatus)) {

                return SagaRecoveryDecision.CONFIRM_INVENTORY;
            }

            if ("SUCCESS".equals(paymentStatus)
                    && "RELEASED".equals(inventoryStatus)) {

                return SagaRecoveryDecision.REFUND_PAYMENT;
            }



            if (("FAILED".equals(paymentStatus)
                    || "REFUNDED".equals(paymentStatus))
                    && "RESERVED".equals(inventoryStatus)) {

                return SagaRecoveryDecision.RELEASE_INVENTORY;
            }

            return SagaRecoveryDecision.MANUAL_INTERVENTION;

        } catch (RuntimeException exception) {

            // Missing records or service communication failures
            // must not trigger an unsafe automatic recovery.
            return SagaRecoveryDecision.MANUAL_INTERVENTION;
        }
    }
}