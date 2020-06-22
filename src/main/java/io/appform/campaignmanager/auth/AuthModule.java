package io.appform.campaignmanager.auth;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.appform.campaignmanager.AppConfig;
import io.appform.dropwizard.multiauth.configs.AuthConfig;
import io.appform.dropwizard.multiauth.model.*;
import io.appform.dropwizard.sharding.DBShardingBundle;
import io.appform.dropwizard.sharding.dao.LookupDao;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class AuthModule extends DropwizardAwareModule<AppConfig> {

    private final DBShardingBundle<AppConfig> shardingBundle;

    public AuthModule(DBShardingBundle<AppConfig> shardingBundle) {
        this.shardingBundle = shardingBundle;
    }

    @Override
    protected void configure() {
        bind(AuthStore.class).to(DBAuthStore.class);
    }

    @Provides
    @Singleton
    public DefaultHandler defaultHandler() {
        return () -> Optional.of(new ServiceUserPrincipal(
                new ServiceUser("__DEFAULT__",
                                ImmutableSet.of(Roles.CAMPAIGN_MANAGEMENT, "AUTH_MANAGEMENT"),
                                null,
                                null),
                Token.DEFAULT));
    }

    @Provides
    @Singleton
    public LookupDao<StoredUser> userDao() {
        return shardingBundle.createParentObjectDao(StoredUser.class);
    }

    @Provides
    @Singleton
    public LookupDao<StoredUserToken> tokenDao() {
        return shardingBundle.createParentObjectDao(StoredUserToken.class);
    }

    @Provides
    @Singleton
    public AuthConfig authConfig(AppConfig appConfig) {
        return appConfig.getAuth();
    }

    @Provides
    @Singleton
    public List<String> roles() {
        return ImmutableList.of(Roles.CAMPAIGN_MANAGEMENT, "AUTH_MANAGEMENT");
    }
}
