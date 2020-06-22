package io.appform.campaignmanager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.okta.jwt.JwtHelper;
import io.appform.campaignmanager.configs.ProviderConfigs;
import io.appform.campaignmanager.data.CampaignStore;
import io.appform.campaignmanager.data.db.DBCampaignStore;
import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.model.StoredNotificationItem;
import io.appform.campaignmanager.okta.OktaAuthenticator;
import io.appform.dropwizard.sharding.DBShardingBundle;
import io.appform.dropwizard.sharding.dao.LookupDao;
import io.appform.dropwizard.sharding.dao.RelationalDao;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;
import lombok.val;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.inject.Singleton;

/**
 *
 */
public class CoreModule extends AbstractModule {

    private final DBShardingBundle<AppConfig> shardingBundle;

    public CoreModule(DBShardingBundle<AppConfig> shardingBundle) {
        this.shardingBundle = shardingBundle;
    }

    @Override
    protected void configure() {
        bind(CampaignStore.class).to(DBCampaignStore.class);
    }

    @Provides
    @Singleton
    public LookupDao<StoredCampaign> campaignDao() {
        return shardingBundle.createParentObjectDao(StoredCampaign.class);
    }

    @Provides
    @Singleton
    public RelationalDao<StoredNotificationItem> notificationDao() {
        return shardingBundle.createRelatedObjectDao(StoredNotificationItem.class);
    }

    @Provides
    @Singleton
    public CloseableHttpClient httpClient(Environment environment, AppConfig appConfig) {
        return new HttpClientBuilder(environment)
                .using(appConfig.getHttpClient())
                .build("notifier");
    }

    @Provides
    @Singleton
    public ProviderConfigs providerConfigs(AppConfig appConfig) {
        return appConfig.getProviderConfigs();
    }

    @Provides
    @Singleton
    public OktaAuthenticator auth(AppConfig configuration) throws Exception {
        val oktaConfig = configuration.getOkta();
        // Configure the JWT Validator, it will validate Okta's JWT access tokens
        JwtHelper helper = new JwtHelper()
                .setIssuerUrl(oktaConfig.getBaseUrl() + "/oauth2/default")
                .setClientId(oktaConfig.getClientId());
        return new OktaAuthenticator(helper.build());
    }
}
