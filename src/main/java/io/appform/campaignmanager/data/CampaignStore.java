package io.appform.campaignmanager.data;

import io.appform.campaignmanager.model.CampaignState;
import io.appform.campaignmanager.model.NotificationState;
import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.model.StoredNotificationItem;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface CampaignStore {
    Optional<StoredCampaign> createCampaign(
            StoredCampaign campaign,
            List<StoredNotificationItem> notificationItems);

    boolean updateCampaignState(String campaignId, CampaignState state);

    Optional<StoredCampaign> getCampaign(String campaignId);

    List<StoredCampaign> listCampaigns(int from, int size);

    Optional<StoredNotificationItem> getNotificationItem(String campaignId, String notificationId);

    List<StoredNotificationItem> listItems(String campaignId, int from, int size);

    List<StoredNotificationItem> itemsWithState(String campaignId, NotificationState state, int from, int size);

    StoredCampaign updateCampaignProgress(
            String campaignId,
            String notificationId,
            int httpStatus,
            String httpResponse,
            String translatedProviderResponse,
            NotificationState notificationState,
            boolean successful);

    StoredCampaign updateNotificationDelivery(
            String campaignId,
            String notificationId,
            String translatedProviderResponse,
            NotificationState notificationState, boolean successful);
}
