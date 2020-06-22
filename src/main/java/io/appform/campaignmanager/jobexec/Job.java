package io.appform.campaignmanager.jobexec;

import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.model.StoredNotificationItem;
import lombok.Value;

import java.util.List;

/**
 *
 */
@Value
public class Job {
    StoredCampaign campaign;
    List<StoredNotificationItem> items;
}
