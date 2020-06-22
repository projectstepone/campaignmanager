package io.appform.campaignmanager.okta;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

/**
 * This filter assigns role to validated user
 */
@Priority(Priorities.AUTHENTICATION)
public class UserAuthorizationFilter implements ContainerRequestFilter {

    public UserAuthorizationFilter() {
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        User principal = SessionUser.take();
        if(null != principal) {
            updateContext(requestContext, principal);
            return;
        }
        throw new IllegalStateException();
    }

    private void updateContext(ContainerRequestContext requestContext, User principal) {
        requestContext.setSecurityContext(new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return requestContext.getSecurityContext().isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }

        });
    }
}
