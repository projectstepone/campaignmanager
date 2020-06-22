package io.appform.campaignmanager.views;

import io.appform.campaignmanager.model.CampaignProgress;
import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.model.StoredNotificationItem;
import io.appform.campaignmanager.utils.Utils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 *
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CampaignView extends BaseView {
    private final StoredCampaign campaign;
    private final CampaignProgress progress;
    private final List<StoredNotificationItem> items;

    @Builder
    public CampaignView(
            StoredCampaign campaign,
            List<StoredNotificationItem> items,
            String error) {
        super("campaign.hbs", error);
        this.campaign = campaign;
        this.progress = Utils.calculateProgress(campaign);
        this.items = items;
    }
}
