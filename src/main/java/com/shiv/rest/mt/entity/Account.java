package com.shiv.rest.mt.entity;


import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
public class Account {
    public static final class AccountBuilder {
        {
            openedAt = Instant.now();
            id = UUID.randomUUID().toString();
            balance = BigDecimal.ZERO;
        }
    }

    @NonNull
    private final String id;
    @NonNull
    private final String sortCode;
    @NonNull
    private final String accountNumber;
    @NonNull
    private final Instant openedAt;
    @NonNull
    private BigDecimal balance;
    private long version;

    public boolean hasAtLeast(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        checkPrecision(amount);
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        checkPrecision(amount);
        balance = balance.add(amount);
    }

    private void checkPrecision(BigDecimal value) {
        Preconditions.checkArgument(value.scale() <= 3, "scale has to be <= 3");
    }

}
