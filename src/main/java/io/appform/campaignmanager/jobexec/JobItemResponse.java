package io.appform.campaignmanager.jobexec;

import io.appform.campaignmanager.model.NotificationState;
import lombok.Value;

/**
 *
 */
@Value
public class JobItemResponse {
    int httpStatus;
    String httpResponse;
    String translatedProviderResponse;
    NotificationState notificationState;
}
