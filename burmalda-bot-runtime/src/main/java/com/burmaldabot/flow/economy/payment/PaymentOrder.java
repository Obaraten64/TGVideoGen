package com.burmaldabot.flow.economy.payment;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "payment_order",
        indexes = {
                @Index(name = "idx_crypto_invoice_id", columnList = "cryptoInvoiceId"),
                @Index(name = "idx_order_uuid", columnList = "orderUuid")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String orderUuid;

    @Column(nullable = false)
    private Long botId;

    @Column(nullable = false)
    private Long telegramUserId;

    @Column(nullable = false)
    private Long chatId;

    @Column(unique = true)
    private Long cryptoInvoiceId;

    @Column(nullable = false)
    private Long tokensAmount;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 16)
    private String asset;

    @Column(length = 512)
    private String payUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    @Version
    private Long version;
}
