package com.shiv.rest.test.mt.service;

import com.shiv.rest.mt.entity.Account;
import com.shiv.rest.mt.entity.SortCodeAccountNumber;
import com.shiv.rest.mt.entity.MoneyTransferRequest;
import com.shiv.rest.mt.entity.MoneyTransferResult;
import com.shiv.rest.mt.repositories.AccountRepo;
import com.shiv.rest.mt.service.MoneyTransfer;
import com.shiv.rest.test.mt.util.TestDbRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MoneyTransferTest {

    public static final String SORT_CODE = "sort";
    @Rule
    public TestDbRule db = new TestDbRule();

    private String shivId;
    private String kumarId;

    @Before
    public void setUp() throws Exception {
        db.getDbi().useHandle(h -> {
            shivId = insertAccount(h, "Shiv", 40).getId();
            kumarId = insertAccount(h, "Kumar", 80).getId();
        });
    }

    @Test
    public void shouldTransferWhenHasBalance() {
        MoneyTransfer transfer = new MoneyTransfer(request("Shiv", "Kumar", "15"), db.getDbi());

        MoneyTransferResult result = transfer.run();

        assertThat(result.isTransferred()).isTrue();
        db.getDbi().useHandle(h -> {
            AccountRepo repo = h.attach(AccountRepo.class);
            Account shiv = repo.find(keyFor("Shiv"));
            Account kumar = repo.find(keyFor("Kumar"));
            assertThat(shiv.getBalance()).isEqualByComparingTo("25");
            assertThat(shiv.getVersion()).isEqualTo(2);
            assertThat(kumar.getBalance()).isEqualByComparingTo("95");
            assertThat(kumar.getVersion()).isEqualTo(2);
        });
    }

    @Test
    public void shouldRegisterTransactionsWhenTransferred() {
        MoneyTransfer transfer = new MoneyTransfer(
                request("Shiv", "Kumar", "15.5", "loan"),
                db.getDbi()
        );

        MoneyTransferResult result = transfer.run();

        assertThat(result.isTransferred()).isTrue();
        db.getDbi().useHandle(h -> {
            Map<String, Object> shivTx = h.createQuery("select * from transactions where account_id = :accountId")
                    .bind("accountId", shivId)
                    .first();
            assertThat(shivTx.get("amount")).isEqualTo(new BigDecimal("15.500"));
            assertThat(shivTx.get("type")).isEqualTo("OUT");
            assertThat(shivTx.get("message")).isEqualTo("loan");

            Map<String, Object> kumarTx = h.createQuery("select * from transactions where account_id = :accountId")
                    .bind("accountId", kumarId)
                    .first();
            assertThat(kumarTx.get("amount")).isEqualTo(new BigDecimal("15.500"));
            assertThat(kumarTx.get("type")).isEqualTo("IN");
            assertThat(kumarTx.get("message")).isEqualTo("loan");
        });
    }

    /*
     * Not using error code constants that are defined in production code.
     * This is because I want this test to fail in case the error codes change.
     * Error codes are a part of our API contract and consumers have to adapt.
     * Until they do we need to ensure backwards compatibility so they need to remain the same.
     * So these tests also act like consumer contract tests
     */

    @Test
    public void shouldNotTransferIfInsufficientBalance() {
        MoneyTransfer transfer = new MoneyTransfer(request("Shiv", "Kumar", "102.4"), db.getDbi());

        MoneyTransferResult result = transfer.run();

        assertThat(result.isTransferred()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("from.insufficient-funds");
        db.getDbi().useHandle(h -> {
            assertThat(txCount(h)).isEqualTo(0);
            AccountRepo repo = h.attach(AccountRepo.class);

            Account kumar = repo.find(keyFor("Kumar"));
            assertThat(kumar.getBalance()).isEqualByComparingTo("80");

            Account shiv = repo.find(keyFor("Shiv"));
            assertThat(shiv.getBalance()).isEqualByComparingTo("40");
        });
    }

    @Test
    public void shouldFailWhenFromDoesNotExist() {
        MoneyTransfer transfer = new MoneyTransfer(request("NotFound", "Shiv", "16"), db.getDbi());

        MoneyTransferResult result = transfer.run();

        assertThat(result.isTransferred()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("from.not-found");
        db.getDbi().useHandle(h -> {
            assertThat(txCount(h)).isEqualTo(0);
            AccountRepo repo = h.attach(AccountRepo.class);
            Account shiv = repo.find(keyFor("Shiv"));
            assertThat(shiv.getBalance()).isEqualByComparingTo("40");
        });
    }

    @Test
    public void shouldFailWhenToDoesNotExist() {
        MoneyTransfer transfer = new MoneyTransfer(request("Shiv", "NotFound", "16"), db.getDbi());

        MoneyTransferResult result = transfer.run();

        assertThat(result.isTransferred()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("to.not-found");
        db.getDbi().useHandle(h -> {
            assertThat(txCount(h)).isEqualTo(0);
            AccountRepo repo = h.attach(AccountRepo.class);
            Account shiv = repo.find(keyFor("Shiv"));
            assertThat(shiv.getBalance()).isEqualByComparingTo("40");
        });
    }

    private Integer txCount(Handle h) {
        return h.createQuery("select count(*) from transactions")
                .map((index, r, ctx) -> r.getInt(1))
                .first();
    }

    private Account insertAccount(Handle handle, String id, int balance) {
        AccountRepo repo = handle.attach(AccountRepo.class);
        Account account = Account.builder()
                .sortCode(SORT_CODE)
                .accountNumber(id)
                .balance(new BigDecimal(balance))
                .version(1)
                .build();
        repo.insert(account);
        return account;
    }

    private MoneyTransferRequest request(String from, String to, String amount) {
        return request(from, to, amount, null);
    }

    private MoneyTransferRequest request(String from, String to, String amount, String message) {
        return MoneyTransferRequest.builder()
                .from(keyFor(from))
                .to(keyFor(to))
                .amount(new BigDecimal(amount))
                .message(message)
                .build();
    }

    private SortCodeAccountNumber keyFor(String from) {
        return SortCodeAccountNumber.builder()
                .sortCode(SORT_CODE)
                .accountNumber(from)
                .build();
    }
}