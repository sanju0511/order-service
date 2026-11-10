package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.entities.SagaExecution;
import com.ecommerce.orderservice.entities.SagaStatus;
import com.ecommerce.orderservice.repository.SagaExecutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SagaRecoveryLockService {
    private final SagaExecutorRepository executorRepository;

    public SagaRecoveryLockService(SagaExecutorRepository executorRepository) {
        this.executorRepository = executorRepository;
    }

    @Transactional
    public String acquireRecoveryLock(Long orderId){


        LocalDateTime now=LocalDateTime.now();
        String token= UUID.randomUUID().toString();
        LocalDateTime lockedUntil=now.plusMinutes(2);
        int updatedRows=executorRepository.claimForRecovery(orderId,
                List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),token,now,lockedUntil);
        if(updatedRows==1){
            return token;
        }
        return null;
    }


    @Transactional(readOnly = true)
    public boolean isClaimValid(Long orderId,String token){
         if(token==null || token.isBlank()){
             return false;
         }
         return executorRepository.isRecoverClaimValid(orderId,token,LocalDateTime.now());
    }


    @Transactional
    public boolean releaseRecoveryLock(Long orderId,String token){
        if(token==null || token.isBlank()){
            return false;
        }
        return executorRepository.releaseRecoveryClaim(orderId,token)==1;
    }

    @Transactional
    public boolean renewRecoveryClaim(Long orderId,String token){

        return executorRepository.renewRecoveryClaim(orderId,
                List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),
                token,LocalDateTime.now(),LocalDateTime.now().plusMinutes(2))==1;
    }



}
