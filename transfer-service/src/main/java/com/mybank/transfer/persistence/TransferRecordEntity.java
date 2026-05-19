package com.mybank.transfer.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfer_record", schema = "transfer")
public class TransferRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_username", nullable = false, length = 128)
    private String senderUsername;

    @Column(name = "recipient_username", nullable = false, length = 128)
    private String recipientUsername;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransferRecordEntity() {
    }

    public TransferRecordEntity(String senderUsername, String recipientUsername, BigDecimal amount) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.amount = amount;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
