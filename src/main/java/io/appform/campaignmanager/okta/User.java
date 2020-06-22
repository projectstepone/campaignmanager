package io.appform.campaignmanager.okta;

import com.okta.jwt.Jwt;
import lombok.Value;

import java.security.Principal;

/**
 *
 */
@Value
public class User implements Principal {

    Jwt accessToken;

    User(Jwt accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String getName() {
        // the 'sub' claim in the access token will be the email address
        return (String) accessToken.getClaims().get("sub");
    }
}
