package com.shiv.rest.mt.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class MoneyTransferServiceConfig extends Configuration {
    @JsonProperty("db")
    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory;

    @NotEmpty
    private String liquibaseChangelog = "migrations.xml";
}
