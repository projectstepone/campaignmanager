package io.appform.campaignmanager.resources;

import io.appform.campaignmanager.AppConfig;
import io.appform.campaignmanager.views.LoginView;
import io.dropwizard.views.View;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Produces(MediaType.TEXT_HTML)
@Path("/")
@Slf4j
public class LoginLogout {

    private final AppConfig appConfig;

    @Inject
    public LoginLogout(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GET
    @Path("/login")
    public View login() {
        return new LoginView(appConfig.getOkta().getBaseUrl(), appConfig.getOkta().getClientId());
    }

}
