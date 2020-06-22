package io.appform.campaignmanager.views;

import io.appform.campaignmanager.model.StoredCampaign;
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
public class HomeView extends BaseView {
    private final List<StoredCampaign> campaigns;
    private final List<String> senders;

    @Builder
    public HomeView(
            List<StoredCampaign> campaigns,
            List<String> senders,
            String error) {
        super("home.hbs", error);
        this.campaigns = campaigns;
        this.senders = senders;
    }
}
