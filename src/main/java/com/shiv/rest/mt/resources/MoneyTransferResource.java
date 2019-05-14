package com.shiv.rest.mt.resources;


import com.shiv.rest.mt.entity.MoneyTransferRequest;
import com.shiv.rest.mt.entity.MoneyTransferResult;
import com.shiv.rest.mt.service.MoneyTransfer;

import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/moneytransfers")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class MoneyTransferResource {

    private final DBI dbi;

    public MoneyTransferResource(DBI dbi) {
        this.dbi = dbi;
    }

    @POST
    public MoneyTransferResult transfer(@Valid @NotNull MoneyTransferRequest request) {
        log.info("Transfer req {}", request);
        return new MoneyTransfer(request, dbi).run();
    }

}
