package com.shiv.rest.mt.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Builder
@Getter
public class Transaction {
    public enum TransactionType {
        IN,
        OUT
    }

    public static final class TransactionBuilder {
        {
            id = UUID.randomUUID().toString();
            issuedAt = Instant.now();
        }
    }

    @NonNull
    private final String id;
    @NonNull
    private final String accountId;
    @NonNull
    private final BigDecimal amount;
    @NonNull
    private final TransactionType type;
    @Getter(AccessLevel.NONE)
    private final String message;
    @NonNull
    private final Instant issuedAt;

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
