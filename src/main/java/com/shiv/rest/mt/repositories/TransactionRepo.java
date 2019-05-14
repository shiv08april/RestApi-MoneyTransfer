package com.shiv.rest.mt.repositories;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.shiv.rest.mt.entity.Transaction;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterMapper(TransactionRepo.TransactionMapper.class)
public interface TransactionRepo {
    @SqlUpdate("insert into transactions (id, account_id, amount, type, message, issued_at) " +
            "values (:id, :accountId, :amount, :type, :message, :issuedAt)")
    void insert(@BindBean Transaction tx);

    class TransactionMapper implements ResultSetMapper<Transaction> {
        @Override
        public Transaction map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return Transaction.builder()
                    .id(r.getString("id"))
                    .accountId(r.getString("account_id"))
                    .amount(r.getBigDecimal("amount"))
                    .type(Transaction.TransactionType.valueOf(r.getString("type")))
                    .message(r.getString("message"))
                    .issuedAt(r.getTimestamp("issued_at").toInstant())
                    .build();
        }
    }
}
