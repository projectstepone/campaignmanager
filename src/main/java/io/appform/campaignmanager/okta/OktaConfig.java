package io.appform.campaignmanager.okta;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 */
@Data
public class OktaConfig {
    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String clientId;
}
