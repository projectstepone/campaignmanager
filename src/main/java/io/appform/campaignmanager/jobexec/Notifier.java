package io.appform.campaignmanager.jobexec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.appform.campaignmanager.configs.ProviderConfigs;
import io.appform.campaignmanager.data.CampaignStore;
import io.appform.campaignmanager.data.NotificationVisitor;
import io.appform.campaignmanager.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Singleton
@Slf4j
public class Notifier {
    private final Provider<CampaignStore> campaignStoreProvider;
    private final Provider<CloseableHttpClient> httpClientProvider;
    private final ProviderConfigs providerConfigs;
    private final ObjectMapper mapper;
    private final ExecutorService executorService;

    @Inject
    public Notifier(
            Provider<CampaignStore> campaignStoreProvider,
            Provider<CloseableHttpClient> httpClientProvider,
            ProviderConfigs providerConfigs,
            ObjectMapper mapper) {
        this.campaignStoreProvider = campaignStoreProvider;
        this.httpClientProvider = httpClientProvider;
        this.providerConfigs = providerConfigs;
        this.mapper = mapper;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public String triggerCampaign(String campaignId) {
        val campaignStore = campaignStoreProvider.get();
        val campaign = campaignStore.getCampaign(campaignId)
                .orElse(null);
        if (null == campaign) {
            return "No campaign found with id: " + campaignId;
        }
        if (!campaign.getState().equals(CampaignState.CREATED)) {
            return String.format("Campaign %s is not in created state. Current state: %s",
                                 campaignId, campaign.getState().name());
        }
        val items = campaignStore.itemsWithState(campaignId, NotificationState.CREATED, 0, Integer.MAX_VALUE);
        if (items.isEmpty()) {
            return "No incomplete items found for campaign: " + campaignId;
        }
        log.info("Found {} items for campaign {}", items.size(), campaignId);
        campaignStore.updateCampaignState(campaignId, CampaignState.IN_PROGRESS);
        executorService.submit(() -> handleJob(new Job(campaign, items)));
        return "";
    }

    private void handleJob(final Job job) {
        val campaign = job.getCampaign();
        val items = job.getItems();
        val handler = new NotificationHandler(httpClientProvider.get(), campaign, providerConfigs, mapper);
        val campaignStore = campaignStoreProvider.get();
        val campaignId = campaign.getCampaignId();
        items.forEach(notificationItem -> {
            val itemResponse = notificationItem.accept(handler);
            val notificationId = notificationItem.getNotificationId();
            log.debug("Campaign: {} Notification: {} result: {}",
                      campaignId,
                      notificationId,
                      itemResponse);
            val updatedCampaign = campaignStore.updateCampaignProgress(
                    campaignId,
                    notificationId,
                    itemResponse.getHttpStatus(),
                    itemResponse.getHttpResponse(),
                    itemResponse.getTranslatedProviderResponse(),
                    itemResponse.getNotificationState(),
                    itemResponse.getNotificationState() == NotificationState.SENT);
            log.info("Updated campaign: {}", updatedCampaign);
        });

    }


    private static class NotificationHandler implements NotificationVisitor<JobItemResponse> {
        private final CloseableHttpClient httpClient;
        private final StoredCampaign campaign;
        private final ProviderConfigs providerConfigs;
        private final ObjectMapper mapper;

        public NotificationHandler(
                CloseableHttpClient httpClient,
                StoredCampaign campaign,
                ProviderConfigs providerConfigs,
                ObjectMapper mapper) {
            this.httpClient = httpClient;
            this.campaign = campaign;
            this.providerConfigs = providerConfigs;
            this.mapper = mapper;
        }

        @Override
        public JobItemResponse visit(StoredSmsNotificationItem sms) {
            val smsSendRequest = new HttpGet();
            CloseableHttpResponse response = null;
            int httpCode = -1;
            String httpResponse = "";
            String translatedProviderResponse = "";
            NotificationState state = NotificationState.TEMPORARY_FAILURE;
            try {
                val uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api-alerts.kaleyra.com")
                        .setPath("/v4/")
                        .addParameter("api_key",
                                      providerConfigs.getConfigs()
                                              .get(ProviderType.KALEYRA_SMS)
                                              .get("apiKey"))
                        .addParameter("method", "sms")
                        .addParameter("message", campaign.getContent())
                        .addParameter("to", sms.getPhone())
                        .addParameter("sender", campaign.getSendAs())
                        .addParameter("unicode", "auto")
                        .addParameter("custom", sms.getNotificationId())
                        .addParameter("dlrurl", callbackUrl(sms))
                        .build();
                log.debug("Calling URI: {}", uri);
                smsSendRequest.setURI(uri);
                response = httpClient.execute(smsSendRequest);
                httpCode = response.getStatusLine().getStatusCode();
                httpResponse = EntityUtils.toString(response.getEntity());
                if (HttpStatus.SC_OK == httpCode) {
                    val responeNode = mapper.readTree(httpResponse);
                    val responseNode = responeNode.at("/data/0/status");
                    if (!responeNode.isMissingNode()) {
                        translatedProviderResponse = responseNode.asText();

                    }
                }
                if (!Strings.isNullOrEmpty(translatedProviderResponse)) {
                    if(translatedProviderResponse.equals("AWAITED-DLR")) {
                        state = NotificationState.SENT;
                    }
                    if(translatedProviderResponse.equals("INV-NUMBER")) {
                        state = NotificationState.PERMANENT_FAILURE;
                    }
                }
            }
            catch (Exception e) {
                log.error("Campaign: " + campaign.getCampaignId() + " Notification: " + sms.getNotificationId()
                                  + " Error sending SMS to phone: " + sms.getPhone(), e);
            }
            finally {
                try {
                    if (null != response) {
                        response.close();
                    }
                }
                catch (IOException e) {
                    log.error("Error closing request", e);
                }
            }
            return new JobItemResponse(httpCode, httpResponse, translatedProviderResponse, state);
        }

        @Override
        public JobItemResponse visit(StoredIVRNotificationItem ivr) {
            val ivrSendRequest = new HttpGet();
            CloseableHttpResponse response = null;
            int httpCode = -1;
            String httpResponse = "";
            String translatedProviderResponse = "";
            NotificationState state = NotificationState.TEMPORARY_FAILURE;
            try {
                val uri = new URIBuilder()
                        .setScheme("https")
                        .setHost("api-voice.kaleyra.com")
                        .setPath("/v1/")
                        .addParameter("api_key", campaign.getAuthKey())
                        .addParameter("method", "dial.click2call")
                        .addParameter("receiver", "ivr:" + campaign.getContent())
                        .addParameter("caller", ivr.getPhone())
                        .addParameter("custom", ivr.getNotificationId())
                        .addParameter("caller_id", campaign.getSendAs())
                        .addParameter("format", "json")
                        .addParameter("callback", callbackUrl(ivr))
                        .build();
                log.debug("Calling URI: {}", uri);
                ivrSendRequest.setURI(uri);
                response = httpClient.execute(ivrSendRequest);
                httpCode = response.getStatusLine().getStatusCode();
                httpResponse = EntityUtils.toString(response.getEntity());
                if (HttpStatus.SC_OK == httpCode) {
                    val responeNode = mapper.readTree(httpResponse);
                    val responseNode = responeNode.at("/status");
                    if (!responeNode.isMissingNode()) {
                        translatedProviderResponse = responseNode.asText();

                    }
                }
                if (!Strings.isNullOrEmpty(translatedProviderResponse)) {
                    if(translatedProviderResponse.equals("200")) {
                        state = NotificationState.SENT;
                    }
                    else {
                        state = NotificationState.PERMANENT_FAILURE;
                    }
                }
            }
            catch (Exception e) {
                log.error("Campaign: " + campaign.getCampaignId() + " Notification: " + ivr.getNotificationId()
                                  + " Error sending SMS to phone: " + ivr.getPhone(), e);
            }
            finally {
                try {
                    if (null != response) {
                        response.close();
                    }
                }
                catch (IOException e) {
                    log.error("Error closing request", e);
                }
            }
            return new JobItemResponse(httpCode, httpResponse, translatedProviderResponse, state);
        }

        @SneakyThrows
        private String callbackUrl(StoredNotificationItem notificationItem) {
            return providerConfigs.getCallbackEndpoint()
                    + "/delivery/" + notificationItem.getCampaignId()
                    + "/" + notificationItem.getNotificationId()
                    + "?status={status}";
        }
    }
}
