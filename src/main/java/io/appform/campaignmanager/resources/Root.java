package io.appform.campaignmanager.resources;

import com.google.common.base.Splitter;
import io.appform.campaignmanager.AppConfig;
import io.appform.campaignmanager.data.CampaignStore;
import io.appform.campaignmanager.jobexec.Notifier;
import io.appform.campaignmanager.model.*;
import io.appform.campaignmanager.utils.Utils;
import io.appform.campaignmanager.views.CampaignSnapshotView;
import io.appform.campaignmanager.views.CampaignView;
import io.appform.campaignmanager.views.HomeView;
import io.appform.campaignmanager.views.ItemsSnapshotView;
import io.appform.dropwizard.multiauth.model.ServiceUserPrincipal;
import io.dropwizard.auth.Auth;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 */
@Produces(MediaType.TEXT_HTML)
@Path("/")
@Slf4j
public class Root {
    private final Provider<CampaignStore> campaignStoreProvider;
    private final Provider<Notifier> notifier;
    private final AppConfig appConfig;

    @Inject
    public Root(
            Provider<CampaignStore> campaignStoreProvider,
            Provider<Notifier> notifier, AppConfig appConfig) {
        this.campaignStoreProvider = campaignStoreProvider;
        this.notifier = notifier;
        this.appConfig = appConfig;
    }

    @GET
    @PermitAll
    public HomeView home() {
        return new HomeView(
                campaignStoreProvider.get().listCampaigns(0, Integer.MAX_VALUE),
                smsSenders(),
                "");
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Path("create/sms")
    @SneakyThrows
    @PermitAll
    public Response createSmsCampaign(
            @FormDataParam("smsContent") final String smsText,
            @FormDataParam("smsSender") final String sender,
            @FormDataParam("smsFile") InputStream fileInputStream,
            @FormDataParam("smsFile") FormDataContentDisposition fileMetaData,
            @Auth final ServiceUserPrincipal user) {
        final String fileName = fileMetaData.getFileName();
        final String campaignId = UUID.randomUUID().toString();
        val outFile = File.createTempFile(campaignId, "csv");
        Files.copy(fileInputStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        val lines = Files.readAllLines(outFile.toPath());
        val numbers = lines
                .stream()
                .filter(line -> line.matches("^\\p{Digit}{10}$"))
                .collect(Collectors.toList());
        if (!numbers.isEmpty()) {
            val campaign = campaignStoreProvider.get()
                    .createCampaign(StoredCampaign.builder()
                                            .campaignId(campaignId)
                                            .name(System.currentTimeMillis() + "-" + fileName)
                                            .createdBy(user.getUser().getId())
                                            .notificationType(NotificationType.SMS)
                                            .content(smsText)
                                            .sendAs(sender)
                                            .itemCount(numbers.size())
                                            .state(CampaignState.CREATED)
                                            .build(),
                                    numbers.stream()
                                            .map(number -> StoredSmsNotificationItem.builder()
                                                    .campaignId(campaignId)
                                                    .notificationId(UUID.randomUUID().toString())
                                                    .phone(number)
                                                    .provider(ProviderType.KALEYRA_SMS)
                                                    .state(NotificationState.CREATED)
                                                    .build())
                                            .collect(Collectors.toList()))
                    .orElse(null);
            log.debug("Campaign saved: {}", campaign);
            return Response.seeOther(URI.create("/campaign/" + campaignId)).build();
        }
        return Response.ok()
                .entity(new HomeView(campaignStoreProvider.get().listCampaigns(0, Integer.MAX_VALUE),
                                     smsSenders(),
                                     "No valid numbers found"))
                .build();
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Path("create/ivr")
    @SneakyThrows
    @PermitAll
    public Response createIVRCampaign(
            @FormDataParam("ivrId") final String ivrId,
            @FormDataParam("ivrKey") final String ivrKey,
            @FormDataParam("ivrSender") final String ivrSender,
            @FormDataParam("ivrFile") InputStream fileInputStream,
            @FormDataParam("ivrFile") FormDataContentDisposition fileMetaData,
            @Auth final ServiceUserPrincipal user) {
        final String fileName = fileMetaData.getFileName();
        final String campaignId = UUID.randomUUID().toString();
        val outFile = File.createTempFile(campaignId, "csv");
        Files.copy(fileInputStream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        val lines = Files.readAllLines(outFile.toPath());
        val numbers = lines
                .stream()
                .filter(line -> line.matches("^\\p{Digit}{10}$"))
                .collect(Collectors.toList());
        if (!numbers.isEmpty()) {
            val campaign = campaignStoreProvider.get()
                    .createCampaign(StoredCampaign.builder()
                                            .campaignId(campaignId)
                                            .name(System.currentTimeMillis() + "-" + fileName)
                                            .createdBy(user.getUser().getId())
                                            .notificationType(NotificationType.IVR)
                                            .content(ivrId)
                                            .sendAs(ivrSender)
                                            .authKey(ivrKey)
                                            .itemCount(numbers.size())
                                            .state(CampaignState.CREATED)
                                            .build(),
                                    numbers.stream()
                                            .map(number -> StoredIVRNotificationItem.builder()
                                                    .campaignId(campaignId)
                                                    .notificationId(UUID.randomUUID().toString())
                                                    .phone(number)
                                                    .provider(ProviderType.KALEYRA_IVR)
                                                    .state(NotificationState.CREATED)
                                                    .build())
                                            .collect(Collectors.toList()))
                    .orElse(null);
            log.debug("Campaign saved: {}", campaign);
            return Response.seeOther(URI.create("/campaign/" + campaignId)).build();
        }
        return Response.ok()
                .entity(new HomeView(campaignStoreProvider.get().listCampaigns(0, Integer.MAX_VALUE),
                                     smsSenders(),
                                     "No valid numbers found"))
                .build();
    }

    @GET
    @Path("campaign/{campaignId}")
    @PermitAll
    public CampaignView getCampaign(
            @PathParam("campaignId") final String campaignId,
            @QueryParam("from") @DefaultValue("0") int from,
            @QueryParam("count") @DefaultValue("1024") int count) {
        val campaignStore = this.campaignStoreProvider.get();
        val campaign = campaignStore.getCampaign(campaignId).orElse(null);
        return new CampaignView(campaign, campaignStore.listItems(campaignId, from, count), "");
    }

    @GET
    @Path("campaign/{campaignId}/snapshot")
    @PermitAll
    public CampaignSnapshotView getCampaignSnapshot(
            @PathParam("campaignId") final String campaignId) {
        val campaignStore = this.campaignStoreProvider.get();
        val campaign = campaignStore.getCampaign(campaignId).orElse(null);
        return new CampaignSnapshotView(campaign, "");
    }

    @GET
    @Path("campaign/{campaignId}/snapshot/items")
    @PermitAll
    public ItemsSnapshotView getItemsSnapshot(
            @PathParam("campaignId") final String campaignId) {
        val campaignStore = this.campaignStoreProvider.get();
        return new ItemsSnapshotView(campaignStore.listItems(campaignId, 0, Integer.MAX_VALUE), "");
    }

    @POST
    @Path("campaign/{campaignId}/trigger")
    @PermitAll
    public Response triggerCampaign(
            @PathParam("campaignId") final String campaignId,
            @QueryParam("from") @DefaultValue("0") int from,
            @QueryParam("count") @DefaultValue("1024") int count) {
        val response = notifier.get().triggerCampaign(campaignId);
        log.info("Campaign setup response for: {} is: {}", campaignId, response);
        return Response.seeOther(URI.create("/campaign/" + campaignId)).build();
    }

    @GET
    @Path("/delivery/{campaignId}/{notificationId}")
    public Response acceptDeliveryReport(
            @PathParam("campaignId") final String campaignId,
            @PathParam("notificationId") final String notificationId,
            @QueryParam("status") final String status) {
        log.info("Response for campaign {} notification {} is: {}",
                 campaignId, notificationId, status);
        val campaignStore = campaignStoreProvider.get();
        val notification = campaignStore.getNotificationItem(campaignId, notificationId).orElse(null);
        if (null == notification) {
            log.warn("No notification found for campaignId: {} notificationId: {}", campaignId, notificationId);
            return Response.ok().build();
        }
        val notificationState = Utils.deliveryState(notification, status);
        val campaign = campaignStore.updateNotificationDelivery(campaignId,
                                                                notificationId,
                                                                status,
                                                                notificationState,
                                                                notificationState.equals(NotificationState.DELIVERED));
        log.info("Updated campaign: {}", campaign);
        return Response.ok().build();
    }

    @GET
    @Path("/download/{campaignId}")
    @Produces("text/csv")
    @PermitAll
    public Response downloadItemsSnapshot(
            @PathParam("campaignId") final String campaignId) {
        val campaignStore = this.campaignStoreProvider.get();
        final StreamingOutput csvStream = output -> {
            Utils.toCsv(campaignStore.listItems(campaignId, 0, Integer.MAX_VALUE), output);
            output.flush();
        };
        return Response
                .ok(csvStream, "text/csv")
                .header("content-disposition", "attachment; filename = " + campaignId + ".csv")
                .build();
    }

    private List<String> smsSenders() {
        return Splitter.on(",")
                .splitToList(appConfig.getProviderConfigs()
                                     .getConfigs()
                                     .get(ProviderType.KALEYRA_SMS)
                                     .get("senders"));
    }

}
