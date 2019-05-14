package com.shiv.rest.mt.service;

import com.shiv.rest.mt.entity.Account;
import com.shiv.rest.mt.entity.Transaction;
import com.shiv.rest.mt.entity.MoneyTransferRequest;
import com.shiv.rest.mt.entity.MoneyTransferResult;
import com.shiv.rest.mt.repositories.AccountRepo;
import com.shiv.rest.mt.repositories.TransactionRepo;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;

@Slf4j
public class MoneyTransfer {
    public static final String FROM_NOT_FOUND = "from.not-found";
    public static final String FROM_INSUFFICIENT_FUNDS = "from.insufficient-funds";
    public static final String TO_NOT_FOUND = "to.not-found";
    public static final String OPTIMISTIC_LOCKING = "optimistic-locking";

    private final MoneyTransferRequest request;
    private final DBI dbi;

    public MoneyTransfer(MoneyTransferRequest request, DBI dbi) {
        this.request = request;
        this.dbi = dbi;
    }

    public MoneyTransferResult run() {
        try {
            return dbi.inTransaction(((handle, status) -> {
                // turns out jdbi throws an exception if setrollbackonly is called explicitly.
                // so we have to rely on throwing exceptions in order to transfer information out of this code block
                // yuck
                AccountRepo accountRepo = handle.attach(AccountRepo.class);

                Account from = accountRepo.find(request.getFrom());
                if (from == null) {
                    throw new TransferFailureException(FROM_NOT_FOUND);
                }
                if (!from.hasAtLeast(request.getAmount())) {
                    throw new TransferFailureException(FROM_INSUFFICIENT_FUNDS);
                }

                Account to = accountRepo.find(request.getTo());
                if (to == null) {
                    throw new TransferFailureException(TO_NOT_FOUND);
                }

                from.debit(request.getAmount());
                to.credit(request.getAmount());

                tryUpdate(accountRepo, from);
                tryUpdate(accountRepo, to);

                TransactionRepo txRepo = handle.attach(TransactionRepo.class);
                txRepo.insert(Transaction.builder()
                        .accountId(from.getId())
                        .type(Transaction.TransactionType.OUT)
                        .amount(request.getAmount())
                        .message(request.getMessage())
                        .build());
                txRepo.insert(Transaction.builder()
                        .accountId(to.getId())
                        .type(Transaction.TransactionType.IN)
                        .amount(request.getAmount())
                        .message(request.getMessage())
                        .build());

                return MoneyTransferResult.success();
            }));
        } catch (CallbackFailedException e) {
            if (e.getCause() instanceof TransferFailureException) {
                TransferFailureException fe = (TransferFailureException) e.getCause();
                return MoneyTransferResult.fail(fe.errorCode);
            }
            throw e;
        }
    }

    private void tryUpdate(AccountRepo accountRepo, Account account) {
        int updateCount = accountRepo.updateWithVersion(account);
        if (updateCount == 0) {
            throw new TransferFailureException(OPTIMISTIC_LOCKING);
        }
    }

    @Value
    private class TransferFailureException extends RuntimeException {
        String errorCode;
    }
}
