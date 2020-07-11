package io.appform.campaignmanager.configs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.appform.campaignmanager.model.ProviderType;
import io.dropwizard.validation.ValidationMethod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

/**
 *
 */
@Data
@Slf4j
public class ProviderConfigs {

    @NotEmpty
    private Map<ProviderType, Map<String, String>> configs;

    @NotEmpty
    private String callbackEndpoint;

    @JsonIgnore
    @ValidationMethod(message = "Provider configs are not valid")
    public boolean isConfigValid() {
        return null != configs
                && configs.entrySet()
                .stream()
                .allMatch(entry -> entry.getKey()
                        .accept(new ProviderType.ProviderTypeVisitor<Boolean>() {
                            @Override
                            public Boolean visitKaleyraSms() {
                                val smsConfigs = entry.getValue();
                                if (!smsConfigs.containsKey("apiKey")) {
                                    log.error("'apiKey' needed for KALEYRA_SMS");
                                    return false;
                                }
                                if (!smsConfigs.containsKey("senders")) {
                                    log.error("'senders' needed for KALEYRA_SMS");
                                    return false;
                                }
                                return true;
                            }

                            @Override
                            public Boolean visitKaleyraIvr() {
                                if (!entry.getValue().containsKey("apiKey")) {
                                    log.error("'apiKey' needed for KALEYRA_IVR");
                                    return false;
                                }
                                return true;
                            }
                        }));
    }
}
