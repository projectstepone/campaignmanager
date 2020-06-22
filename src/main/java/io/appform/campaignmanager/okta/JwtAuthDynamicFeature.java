package io.appform.campaignmanager.okta;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;

/**
 *
 */
@Provider
public class JwtAuthDynamicFeature extends AuthDynamicFeature {
    @Inject
    public JwtAuthDynamicFeature(
            Environment environment) {
        super(new UserAuthorizationFilter());
        if(null != environment) {
            environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
            environment.jersey().register(RolesAllowedDynamicFeature.class);
        }
    }
}
