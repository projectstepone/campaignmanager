package io.appform.campaignmanager.data.db;

import io.appform.campaignmanager.data.CampaignStore;
import io.appform.campaignmanager.model.CampaignState;
import io.appform.campaignmanager.model.NotificationState;
import io.appform.campaignmanager.model.StoredCampaign;
import io.appform.campaignmanager.model.StoredNotificationItem;
import io.appform.dropwizard.sharding.dao.LookupDao;
import io.appform.dropwizard.sharding.dao.RelationalDao;
import lombok.SneakyThrows;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stores campaign to relational DB
 */
@Singleton
public class DBCampaignStore implements CampaignStore {

    private final LookupDao<StoredCampaign> campaignDao;
    private final RelationalDao<StoredNotificationItem> itemDao;

    @Inject
    public DBCampaignStore(
            LookupDao<StoredCampaign> campaignDao,
            RelationalDao<StoredNotificationItem> itemDao) {
        this.campaignDao = campaignDao;
        this.itemDao = itemDao;
    }

    @Override
    @SneakyThrows
    public Optional<StoredCampaign> createCampaign(
            StoredCampaign campaign,
            List<StoredNotificationItem> notificationItems) {
        return Optional.of(campaignDao.saveAndGetExecutor(campaign)
                                   .saveAll(itemDao, c -> notificationItems)
                                   .execute());
    }

    @Override
    public boolean updateCampaignState(String campaignId, CampaignState state) {
        return campaignDao.update(campaignId, storedCampaign ->
                storedCampaign
                        .map(c -> {
                            c.setState(state);
                            return c;
                        })
                        .orElse(null));
    }

    @Override
    @SneakyThrows
    public Optional<StoredCampaign> getCampaign(String campaignId) {
        return campaignDao.get(campaignId);
    }

    @Override
    public List<StoredCampaign> listCampaigns(int from, int size) {
        return campaignDao.scatterGather(DetachedCriteria.forClass(StoredCampaign.class)
                                                 .addOrder(Order.desc("created")))
                            .stream()
                            .sorted(Comparator.comparing(StoredCampaign::getCreated).reversed())
                            .collect(Collectors.toList());
    }

    @Override
    @SneakyThrows
    public Optional<StoredNotificationItem> getNotificationItem(String campaignId, String notificationId) {
        return itemDao.select(
                campaignId,
                DetachedCriteria.forClass(StoredNotificationItem.class)
                .add(Restrictions.eq("campaignId", campaignId))
                .add(Restrictions.eq("notificationId", notificationId)),
                0,
                1)
                .stream()
                .findAny();
    }

    @Override
    @SneakyThrows
    public List<StoredNotificationItem> listItems(String campaignId, int from, int size) {
        return getStoredNotificationItems(
                campaignId,
                DetachedCriteria.forClass(StoredNotificationItem.class)
                        .add(Restrictions.eq("campaignId", campaignId)),
                from,
                size);
    }


    @Override
    @SneakyThrows
    public List<StoredNotificationItem> itemsWithState(
            String campaignId, NotificationState state, int from, int size) {
        return getStoredNotificationItems(
                campaignId,
                DetachedCriteria.forClass(StoredNotificationItem.class)
                        .add(Restrictions.eq("campaignId", campaignId))
                        .add(Restrictions.eq("state", state)),
                from,
                size);
    }

    @Override
    public StoredCampaign updateCampaignProgress(
            String campaignId,
            String notificationId,
            int httpStatus,
            String httpResponse,
            String translatedProviderResponse,
            NotificationState notificationState,
            boolean successful) {
        return campaignDao.lockAndGetExecutor(campaignId)
                .filter(c -> c.getState().equals(CampaignState.IN_PROGRESS))
                .update(itemDao,
                        DetachedCriteria.forClass(StoredNotificationItem.class)
                                .add(Restrictions.eq("campaignId", campaignId))
                                .add(Restrictions.eq("notificationId", notificationId)),
                                     i -> {
                                         i.setHttpStatus(httpStatus);
                                         i.setHttpResponse(httpResponse);
                                         i.setTranslatedProviderResponse(translatedProviderResponse);
                                         i.setState(notificationState);
                                         return i;
                                     },
                                     () -> false)
                .mutate(c -> {
                    c.setCompleted(c.getCompleted() + 1);
                    c.setSent(c.getSent() + (notificationState.equals(NotificationState.SENT) ? 1 : 0));
                    c.setFailures(c.getFailures() + (!successful? 1 : 0));
                    c.setPermanentFailures(
                            c.getPermanentFailures()
                                    + (notificationState.equals(NotificationState.PERMANENT_FAILURE) ? 1 : 0));
                    c.setTemporaryFailures(
                            c.getTemporaryFailures()
                                    + (notificationState.equals(NotificationState.TEMPORARY_FAILURE) ? 1 : 0));
                    c.setState(c.getCompleted() == c.getItemCount()
                               ? CampaignState.COMPLETED
                               : c.getState());
                })
                .execute();
    }

    @Override
    public StoredCampaign updateNotificationDelivery(
            String campaignId,
            String notificationId,
            String translatedProviderResponse,
            NotificationState notificationState,
            boolean successful) {
        return campaignDao.lockAndGetExecutor(campaignId)
                .update(itemDao,
                        DetachedCriteria.forClass(StoredNotificationItem.class)
                                .add(Restrictions.eq("campaignId", campaignId))
                                .add(Restrictions.eq("notificationId", notificationId)),
                        i -> {
                            i.setTranslatedProviderResponse(translatedProviderResponse);
                            i.setState(notificationState);
                            return i;
                        },
                        () -> false)
                .mutate(c -> {
                    c.setSent(c.getSent() + (notificationState.equals(NotificationState.SENT) ? 1 : 0));
                    c.setDelivered(c.getDelivered() + (successful ? 1 : 0));
                    c.setDeliveryFailures(c.getDeliveryFailures() + (!successful? 1 : 0));
                    c.setPermanentFailures(
                            c.getPermanentFailures()
                                    + (notificationState.equals(NotificationState.PERMANENT_FAILURE) ? 1 : 0));
                    c.setTemporaryFailures(
                            c.getTemporaryFailures()
                                    + (notificationState.equals(NotificationState.TEMPORARY_FAILURE) ? 1 : 0));
                })
                .execute();
    }

    private List<StoredNotificationItem> getStoredNotificationItems(
            String campaignId, DetachedCriteria criteria, int from, int size) throws Exception {
        return itemDao.select(campaignId, criteria, from, size);
    }

}
