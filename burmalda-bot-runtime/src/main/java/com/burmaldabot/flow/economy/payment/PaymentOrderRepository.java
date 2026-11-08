package com.burmaldabot.flow.economy.payment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderUuid(String orderUuid);

    Optional<PaymentOrder> findByCryptoInvoiceId(Long cryptoInvoiceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentOrder p WHERE p.cryptoInvoiceId = :cryptoInvoiceId")
    Optional<PaymentOrder> findForUpdateByCryptoInvoiceId(@Param("cryptoInvoiceId") Long cryptoInvoiceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentOrder p WHERE p.orderUuid = :orderUuid")
    Optional<PaymentOrder> findForUpdateByOrderUuid(@Param("orderUuid") String orderUuid);
}
