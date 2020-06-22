package io.appform.campaignmanager.okta;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * This filter validates the token
 */
@Priority(Priorities.AUTHENTICATION)
@WebFilter("/*")
@Slf4j
public class UserAuthenticationFilter implements Filter {
    private static final Set<String> WHITELISTED_PATTERNS = ImmutableSet.<String>builder()
            .add("/login")
            .add("/static/")
            .add("/delivery/")
        .build();
    private final Provider<OktaAuthenticator> authenticator;

    @Inject
    public UserAuthenticationFilter(
            Provider<OktaAuthenticator> authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        final String requestURI = httpRequest.getRequestURI();
        if(WHITELISTED_PATTERNS.stream().anyMatch(requestURI::startsWith)) {
            chain.doFilter(request, response);
            return;
        }
        val jwt = getTokenFromCookieOrHeader(httpRequest).orElse(null);
        if(null != jwt) {
            try {
                val principal = authenticator.get()
                        .authenticate(jwt).orElse(null);
                if(null != principal) {
                    SessionUser.put(principal);
                    chain.doFilter(request, response);
                    return;
                }
            }
            catch (AuthenticationException e) {
                log.error("Jwt validation failure: ", e);
            }
        }
        val referrer = httpRequest.getHeader(org.apache.http.HttpHeaders.REFERER);
        val source = Strings.isNullOrEmpty(referrer) ? requestURI : referrer;
        httpResponse.addCookie(new Cookie("redirection", source));
        httpResponse.sendRedirect("/login");
    }

    @Override
    public void destroy() {

    }

    private Optional<String> getTokenFromCookieOrHeader(HttpServletRequest servletRequest) {
        val tokenFromHeader = getTokenFromHeader(servletRequest);
        return tokenFromHeader.isPresent() ? tokenFromHeader : getTokenFromCookie(servletRequest);
    }

    private Optional<String> getTokenFromHeader(HttpServletRequest servletRequest) {
        val header = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            int space = header.indexOf(' ');
            if (space > 0) {
                final String method = header.substring(0, space);
                if ("Bearer".equalsIgnoreCase(method)) {
                    final String rawToken = header.substring(space + 1);
                    return Optional.of(rawToken);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getTokenFromCookie(HttpServletRequest request) {
        val cookies = request.getCookies();
        if(null != cookies && cookies.length != 0) {
            val token = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("sone-token")).findAny().orElse(null);
            if(null != token) {
                return Optional.of(token.getValue());
            }
        }
        return Optional.empty();
    }

}
