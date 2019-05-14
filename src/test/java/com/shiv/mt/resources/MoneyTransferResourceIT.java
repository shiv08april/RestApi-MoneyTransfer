package com.shiv.mt.resources;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.shiv.rest.mt.entity.MoneyTransferResult;
import com.shiv.rest.mt.resources.MoneyTransferResource;

import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit.ResourceTestRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class MoneyTransferResourceIT {

    private static DBI dbi = mock(DBI.class);

    static {
        // because of stupid ResourceTestRule
        BootstrapLogging.bootstrap(Level.DEBUG);
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new MoneyTransferResource(dbi))
            .build();


    @After
    public void tearDown() throws Exception {
        Mockito.reset(dbi);
    }

    @Test
    public void shouldAcceptValidRequest() {
        when(dbi.inTransaction(any())).thenReturn(MoneyTransferResult.fail("some-error"));

        Response response = resources.target("/moneytransfers")
                .request()
                .post(Entity.entity(ImmutableMap.of(
                        "from", ImmutableMap.of(
                                "sortCode", "sss",
                                "accountNumber", "sss"
                        ),
                        "to", ImmutableMap.of(
                                "sortCode", "sss",
                                "accountNumber", "sss"
                        ),
                        "amount", new BigDecimal("14.000")
                        ),
                        MediaType.APPLICATION_JSON));

        String responseJson = response.readEntity(String.class);
        log.info("HTTP {}: {}", response.getStatus(), responseJson);
        assertThat((Object) JsonPath.read(responseJson, "$.transferred")).isEqualTo(false);
        assertThat((Object) JsonPath.read(responseJson, "$.errorCode")).isEqualTo("some-error");
    }
}