package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.entities.SagaExecution;
import com.ecommerce.orderservice.entities.SagaStatus;
import com.ecommerce.orderservice.repository.SagaExecutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SagaRecoveryScheduler {
    private static final Logger log =
            LoggerFactory.getLogger(SagaRecoveryScheduler.class);

    private final SagaExecutorRepository repository;

    public SagaRecoveryScheduler(SagaExecutorRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 60000)
    public void findUnfinishedStages(){
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(2);
        List<SagaExecution> unfinishedStages=repository.findByStatusInAndUpdatedAtBefore(
                List.of(SagaStatus.RUNNING,SagaStatus.COMPENSATING),cutoffTime
        );
        for (SagaExecution saga:unfinishedStages) {
            log.warn(
                    "Unfinished Saga detected | Order ID: {} | Step: {} | Status: {}",
                    saga.getOrderId(),
                    saga.getCurrentStep(),
                    saga.getStatus()
            );
        }
    }
}
