package com.shiv.rest.mt;

import com.shiv.rest.mt.configuration.MoneyTransferServiceConfig;
import com.shiv.rest.mt.resources.MoneyTransferResource;
import com.shiv.rest.mt.sys.LiquibaseMigrateOnBoot;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class MoneyTransferService extends Application<MoneyTransferServiceConfig> {
    public static void main(String[] args) throws Exception {
        new MoneyTransferService().run(args);
    }

    @Override
    public String getName() {
        return "transfer-service";
    }

    @Override
    public void initialize(Bootstrap<MoneyTransferServiceConfig> bootstrap) {}

    @Override
    public void run(MoneyTransferServiceConfig transferServiceConfig, Environment environment) throws Exception {
        // TODO
        DBIFactory factory = new DBIFactory();
        DBI dbi = factory.build(environment, transferServiceConfig.getDataSourceFactory(), "dbi");
        environment.jersey().register(new MoneyTransferResource(dbi));

        environment.lifecycle().manage(new LiquibaseMigrateOnBoot(
                () -> LiquibaseMigrateOnBoot.create(dbi.open(), Handle::getConnection),
                transferServiceConfig.getLiquibaseChangelog()
        ));
    }

}
