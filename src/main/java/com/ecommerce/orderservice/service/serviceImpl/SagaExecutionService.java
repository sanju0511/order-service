package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.dto.SagaResponse;
import com.ecommerce.orderservice.entities.SagaExecution;
import com.ecommerce.orderservice.entities.SagaStatus;
import com.ecommerce.orderservice.entities.SagaStep;
import com.ecommerce.orderservice.repository.SagaExecutorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SagaExecutionService {
    private final SagaExecutorRepository repository;

    public SagaExecutionService(SagaExecutorRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SagaExecution startSaga(Long orderId){
        SagaExecution sagaExecution=new SagaExecution();
        sagaExecution.setOrderId(orderId);
        sagaExecution.setCurrentStep(SagaStep.ORDER_CREATED);
        sagaExecution.setStatus(SagaStatus.RUNNING);
        return repository.save(sagaExecution);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSaga(Long orderId,SagaStep step){
        SagaExecution sagaExecution=getOrderDetails(orderId);
        sagaExecution.setCurrentStep(step);
         repository.save(sagaExecution);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSaga(Long orderId){
        SagaExecution sagaExecution=getOrderDetails(orderId);
        sagaExecution.setCurrentStep(SagaStep.COMPLETED);
        sagaExecution.setStatus(SagaStatus.COMPLETED);
        repository.save(sagaExecution);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensating(Long orderId,String reason){
        SagaExecution sagaExecution=getOrderDetails(orderId);

        sagaExecution.setStatus(SagaStatus.COMPENSATING);
        sagaExecution.setFailureReason(reason);
        repository.save(sagaExecution);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long orderId,String reason){
        SagaExecution sagaExecution=getOrderDetails(orderId);

        sagaExecution.setStatus(SagaStatus.FAILED);
        sagaExecution.setFailureReason(reason);
        repository.save(sagaExecution);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCancelled(Long orderId, String reason) {

        SagaExecution saga = getOrderDetails(orderId);

        saga.setStatus(SagaStatus.CANCELLED);
        saga.setFailureReason(reason);

        repository.save(saga);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStep(Long orderId, SagaStep step,String token) {

        int updatedRows= repository.updateStepWithRecoveryLock(orderId, List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),
                token, LocalDateTime.now(),step);

        if(updatedRows!=1){
            throw new IllegalStateException("Cannot update saga.Recovery claim is invalid or expired for order :"+orderId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSagaWithRecoveryLock(Long orderId,String token) {

        int updatedRows= repository.completeSagaWithRecoveryLock(orderId, List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),
                token, LocalDateTime.now(),SagaStep.COMPLETED,SagaStatus.COMPLETED);

        if(updatedRows!=1){
            throw new IllegalStateException("Cannot complete saga.Recovery claim is invalid or expired for order :"+orderId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelSagaWithRecoveryLock(Long orderId,String token) {

        int updatedRows= repository.cancelSagaWithRecoveryLock(orderId, List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),
                token, LocalDateTime.now(),SagaStatus.CANCELLED);

        if(updatedRows!=1){
            throw new IllegalStateException("Cannot cancel saga.Recovery claim is invalid or expired for order :"+orderId);
        }
    }

    @Transactional(readOnly = true)
    public SagaResponse getSagaByOrderId(Long orderId) {

        SagaExecution saga = getOrderDetails(orderId);

        return new SagaResponse(
                saga.getOrderId(),
                saga.getCurrentStep(),
                saga.getStatus(),
                saga.getFailureReason(),
                saga.getCreatedAt(),
                saga.getUpdatedAt()
        );
    }
    public SagaExecution getOrderDetails(Long orderId){
        return repository.findByOrderId(orderId).orElseThrow(()->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found :"+orderId)
        );
    }
}
