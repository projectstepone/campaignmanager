package io.appform.campaignmanager.model;

import lombok.Value;

/**
 *
 */
@Value
public class CampaignProgress {
    double sent;
    double failed;
    double delivered;
    double deliveryFailures;
}
