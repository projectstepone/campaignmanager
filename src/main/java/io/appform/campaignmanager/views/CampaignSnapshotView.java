package io.appform.campaignmanager.views;

import io.appform.campaignmanager.model.CampaignProgress;
import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.utils.Utils;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CampaignSnapshotView extends BaseView {
    private final StoredCampaign campaign;
    private final CampaignProgress progress;

    @Builder
    public CampaignSnapshotView(
            StoredCampaign campaign,
            String error) {
        super("campaignsnapshot.hbs", error);
        this.campaign = campaign;
        this.progress = Utils.calculateProgress(campaign);
    }
}
