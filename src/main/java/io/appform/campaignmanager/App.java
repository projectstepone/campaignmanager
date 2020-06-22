package io.appform.campaignmanager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.inject.Stage;
import io.appform.campaignmanager.handlebars.HandlebarsHelperBundle;
import io.appform.campaignmanager.handlebars.HandlebarsHelpers;
import io.appform.dropwizard.sharding.DBShardingBundle;
import io.appform.dropwizard.sharding.config.ShardedHibernateFactory;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import lombok.val;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Main application class
 */
public class App extends Application<AppConfig> {
    private HandlebarsHelperBundle<AppConfig> handlebarsBundle = new HandlebarsHelperBundle<AppConfig>() {
        @Override
        protected void configureHandlebars(AppConfig configuration) {
            //nothing to configure
        }
    };


    @Override
    public void initialize(Bootstrap<AppConfig> bootstrap) {
        super.initialize(bootstrap);
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                                               new EnvironmentVariableSubstitutor(true)));
        final DBShardingBundle<AppConfig> dbShardingBundle = new DBShardingBundle<AppConfig>("io.appform.campaignmanager.model") {
            @Override
            protected ShardedHibernateFactory getConfig(AppConfig appConfig) {
                return appConfig.getDb();
            }
        };

        bootstrap.addBundle(dbShardingBundle);
        bootstrap.addBundle(new AssetsBundle("/assets", "/static"));
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(handlebarsBundle);
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(
                GuiceBundle.<AppConfig>builder()
                .enableAutoConfig(getClass().getPackage().getName())
                .modules(new CoreModule(dbShardingBundle))
                .useWebInstallers()
                .printDiagnosticInfo()
                .build(Stage.PRODUCTION));

        HandlebarsHelperBundle.registerHelpers(new HandlebarsHelpers());
    }

    @Override
    public void run(AppConfig configuration, Environment environment) throws Exception {
        val objectMapper = environment.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.run(args);
    }

}
