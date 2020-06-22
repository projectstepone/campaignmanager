package io.appform.campaignmanager.views;


import io.dropwizard.views.View;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 *
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoginView extends View {
    String endpoint;
    String clientId;
    public LoginView(String endpoint, String clientId) {
        super("login.hbs");
        this.endpoint = endpoint;
        this.clientId = clientId;
    }
}
