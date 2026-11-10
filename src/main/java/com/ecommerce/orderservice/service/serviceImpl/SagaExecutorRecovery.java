package com.ecommerce.orderservice.service.serviceImpl;


import com.ecommerce.orderservice.client.InventoryFeignClient;
import com.ecommerce.orderservice.client.PaymentFeignClient;
import com.ecommerce.orderservice.dto.SagaRecoveryDecision;
import com.ecommerce.orderservice.dto.StockOperationRequest;
import com.ecommerce.orderservice.entities.Order;
import com.ecommerce.orderservice.entities.OrderStatus;
import com.ecommerce.orderservice.entities.SagaStep;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.repository.OrderServiceRepository;
import org.springframework.stereotype.Service;

@Service
public class SagaExecutorRecovery {
    private final SagaExecutionService executionService;
    private final SagaRecoveryDecisionService decisionService;
    private final OrderServiceRepository repository;
    private final InventoryFeignClient inventoryFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final SagaRecoveryLockService lockService;


    public SagaExecutorRecovery(SagaExecutionService executionService, SagaRecoveryDecisionService decisionService, OrderServiceRepository repository, InventoryFeignClient inventoryFeignClient, PaymentFeignClient paymentFeignClient, SagaRecoveryLockService lockService) {
        this.executionService = executionService;
        this.decisionService = decisionService;
        this.repository = repository;
        this.inventoryFeignClient = inventoryFeignClient;
        this.paymentFeignClient = paymentFeignClient;
        this.lockService = lockService;
    }

    public String recoverOrder(Long orderId) {
        String token = lockService.acquireRecoveryLock(orderId);
        if (token == null) {
            return "Recovery skipped:Saga is already claimed or is not eligible.";
        }
        try {
            if (!lockService.isClaimValid(orderId, token)) {
                return "Recovery stopped:Claim is no longer valid:";
            }


            SagaRecoveryDecision decision = decisionService.decide(orderId);
            Order order = repository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order Not found:" + orderId));
            StockOperationRequest quantity = new StockOperationRequest(order.getQuantity());
            switch (decision) {
                case COMPLETE_ORDER:
                    order.setOrderStatus(OrderStatus.CONFIRMED);
                    repository.save(order);
                    validateRenewClaim(orderId,token);
                    executionService.completeSagaWithRecoveryLock(orderId,token);

                    return "Order Completed Successfully";
                case CONFIRM_INVENTORY:
                    validateRenewClaim(orderId,token);
                    inventoryFeignClient.confirmStock(order.getProductId(), orderId, quantity);

                    validateRenewClaim(orderId,token);
                    executionService.updateStep(orderId,SagaStep.INVENTORY_CONFIRMED,token);

                    validateRenewClaim(orderId,token);
                    order.setOrderStatus(OrderStatus.CONFIRMED);
                    repository.save(order);

                    executionService.completeSagaWithRecoveryLock(orderId,token);
                    return "Inventory Confirmed and  Order completed";
                case RELEASE_INVENTORY:
                    validateRenewClaim(orderId,token);
                    inventoryFeignClient.releaseStock(order.getProductId(), orderId, quantity);

                    validateRenewClaim(orderId,token);
                    executionService.updateStep(orderId,SagaStep.INVENTORY_RELEASED,token);

                    order.setOrderStatus(OrderStatus.CANCELLED);
                    repository.save(order);

                    validateRenewClaim(orderId,token);
                    executionService.cancelSagaWithRecoveryLock(orderId, token);

                    return "inventory released and Order cancelled successfully";
                case REFUND_PAYMENT:
                    validateRenewClaim(orderId,token);
                    paymentFeignClient.refundPayment(orderId);

                    validateRenewClaim(orderId,token);
                    executionService.updateStep(orderId,SagaStep.REFUND_COMPLETED,token);
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    repository.save(order);

                    validateRenewClaim(orderId,token);
                    executionService.cancelSagaWithRecoveryLock(orderId, token);
                    return "Payment Refunded and order cancelled";
                case MANUAL_INTERVENTION:
                default:
                    return "Manual intervention is required for order:" + orderId;
            }

        }finally {
            lockService.releaseRecoveryLock(orderId,token);
        }
    }

    private void validateRenewClaim(Long orderId,String token){
        boolean renew= lockService.renewRecoveryClaim(orderId, token);
        if(!renew){
            throw new IllegalStateException("Recovery Claim expired or ownership lost for order: "+orderId);
        }

    }
}
