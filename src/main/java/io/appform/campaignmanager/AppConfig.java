package io.appform.campaignmanager;

import io.appform.campaignmanager.configs.ProviderConfigs;
import io.appform.dropwizard.multiauth.configs.AuthConfig;
import io.appform.dropwizard.sharding.config.ShardedHibernateFactory;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppConfig extends Configuration {

    @Valid
    @NotNull
    private ShardedHibernateFactory db = new ShardedHibernateFactory();

    @Valid
    @NotNull
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @Valid
    @NotNull
    private AuthConfig auth = new AuthConfig();

    @Valid
    @NotNull
    private ProviderConfigs providerConfigs;
}
