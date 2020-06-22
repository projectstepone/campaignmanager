package io.appform.campaignmanager.okta;

import com.okta.jwt.JoseException;
import com.okta.jwt.JwtVerifier;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

/**
 *
 */
@Slf4j
public class OktaAuthenticator implements Authenticator<String, User> {

    private final JwtVerifier jwtVerifier;

    public OktaAuthenticator(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Optional<User> authenticate(String accessToken) throws AuthenticationException {

        try {
            val jwt = jwtVerifier.decodeAccessToken(accessToken);
            // if we made it this far we have a valid jwt
            return Optional.of(new User(jwt));
        } catch (JoseException e) {
            throw new AuthenticationException(e);
        }
    }
}

