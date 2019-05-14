package com.shiv.rest.mt.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoneyTransferResult {
    public static MoneyTransferResult fail(String errorCode) {
        return new MoneyTransferResult(false, errorCode);
    }

    public static MoneyTransferResult success() {
        return new MoneyTransferResult(true, null);
    }

    boolean transferred;
    String errorCode;

}
