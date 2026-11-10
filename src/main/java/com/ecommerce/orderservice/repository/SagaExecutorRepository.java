package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entities.SagaExecution;
import com.ecommerce.orderservice.entities.SagaStatus;
import com.ecommerce.orderservice.entities.SagaStep;
import jakarta.persistence.LockModeType;
import org.springframework.boot.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SagaExecutorRepository extends JpaRepository<SagaExecution,Long> {

    Optional<SagaExecution> findByOrderId(Long orderId);

    List<SagaExecution> findByStatusInAndUpdatedAtBefore(
            List<SagaStatus> statuses,
            LocalDateTime cutoffTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      select se from SagaExecution se where se.orderId=:orderId
""")
    Optional<SagaExecution> findByOrderIdForUpdate(@Param("orderId") Long orderId);



    @Modifying
    @Query("""
   Update SagaExecution s Set s.recoveryLockToken=:token,
   s.recoveryLockedUntil=:lockedUntil where s.orderId=:orderId
    and  s.status In :statuses 
    and (s.recoveryLockedUntil is null or s.recoveryLockedUntil<:now)
""")
    int claimForRecovery(@Param("orderId") Long orderId,
                         @Param("statuses") List<SagaStatus> statuses,
                         @Param("token") String token,
                         @Param("now") LocalDateTime now,
                         @Param("lockedUntil") LocalDateTime locedUntil);

    @Query("""
     Select count(s)>0 from SagaExecution s where s.orderId=:orderId and
       s.recoveryLockToken=:token and
       s.recoveryLockedUntil > :now
""")
    boolean isRecoverClaimValid(@Param("orderId") Long orderId,
                                @Param("token") String token,
                                @Param("now") LocalDateTime now);


    @Modifying
    @Query("""
 update SagaExecution s set s.recoveryLockToken=null,
                            s.recoveryLockedUntil=null where 
                            
                       s.orderId=:orderId and s.recoveryLockToken=:token
""")
    int releaseRecoveryClaim(@Param("orderId") Long orderId,
                             @Param("token") String token);




    @Modifying
    @Query("""
   Update SagaExecution s Set s.recoveryLockToken=:token,
   s.recoveryLockedUntil=:newExpiry where s.orderId=:orderId
    and s.recoveryLockToken=:token
    and  s.status In :statuses 
    and s.recoveryLockedUntil>:now
""")
    int renewRecoveryClaim(@Param("orderId") Long orderId,
                         @Param("statuses") List<SagaStatus> statuses,
                         @Param("token") String token,
                         @Param("now") LocalDateTime now,
                         @Param("newExpiry") LocalDateTime newExpiry);


    @Modifying
    @Query("""
   Update SagaExecution s Set s.currentStep=:step,
   s.updatedAt=:now where s.orderId=:orderId
    and s.recoveryLockToken=:token
    and  s.status In :statuses
    and s.recoveryLockedUntil>:now
""")
    int updateStepWithRecoveryLock(@Param("orderId") Long orderId,
                           @Param("statuses") List<SagaStatus> statuses,
                           @Param("token") String token,
                           @Param("now") LocalDateTime now,
                           @Param("step") SagaStep step);



    @Modifying
    @Query("""
   Update SagaExecution s Set s.currentStep=:step,
     s.status=:status,
     s.recoveryLockeToken=null,
     s.recoveryLockedUntil=null,
   s.updatedAt=:now where s.orderId=:orderId
    and s.recoveryLockToken=:token
    and  s.status In :statuses
    and s.recoveryLockedUntil>:now
""")
    int completeSagaWithRecoveryLock(@Param("orderId") Long orderId,
                                   @Param("statuses") List<SagaStatus> statuses,
                                   @Param("token") String token,
                                   @Param("now") LocalDateTime now,
                                   @Param("step") SagaStep step,
                                     @Param("status") SagaStatus status);


    @Modifying
    @Query("""
   Update SagaExecution s Set
     s.status=:status,
     s.recoveryLockeToken=null,
     s.recoveryLockedUntil=null,
   s.updatedAt=:now
   where s.orderId=:orderId
    and s.recoveryLockToken=:token
    and  s.status In :statuses
    and s.recoveryLockedUntil>:now
""")
    int cancelSagaWithRecoveryLock(@Param("orderId") Long orderId,
                                     @Param("statuses") List<SagaStatus> statuses,
                                     @Param("token") String token,
                                     @Param("now") LocalDateTime now,
                                     @Param("status") SagaStatus status);

}

