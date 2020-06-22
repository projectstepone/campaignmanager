package io.appform.campaignmanager.utils;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.appform.campaignmanager.data.NotificationVisitor;
import io.appform.campaignmanager.model.*;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@UtilityClass
@Slf4j
public class Utils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");

    public static CampaignProgress calculateProgress(final StoredCampaign campaign) {
        if(null == campaign) {
            return new CampaignProgress(0,0,0, 0);
        }
        return new CampaignProgress(
                ((double)campaign.getSent()/campaign.getItemCount()) * 100.0,
                ((double)campaign.getFailures()/campaign.getItemCount()) * 100.0,
                campaign.getSent() > 0
                    ? ((double)campaign.getDelivered()/campaign.getSent()) * 100.0
                    : 0,
                campaign.getSent() > 0
                    ? ((double)campaign.getDeliveryFailures()/campaign.getSent()) * 100.0
                    : 0);
    }

    public static NotificationState deliveryState(StoredNotificationItem notificationItem, String status) {
        return notificationItem.accept(new NotificationVisitor<NotificationState>() {
            @Override
            public NotificationState visit(StoredSmsNotificationItem sms) {
                //Based on: http://messaging.kaleyra.com/support/solutions/articles/3000091772-sms-status-codes
                switch (status) {
                    case "DELIVRD":
                        return NotificationState.DELIVERED;
                    case "SERIES-BLOCK":
                    case "INV-NUMBER":
                    case "SPAM":
                    case "SNDRID-NOT-ALLOTED":
                    case "BLACKLIST":
                    case "TEMPLATE-NOT-FOUND":
                    case "INV-TEMPLATE-MATCH":
                    case "SENDER-ID-NOT-FOUND":
                    case "DNDNUMB":
                    case "INVALID-SUB":
                    case "ABSENT-SUB":
                    case "HANDSET-ERR":
                    case "BARRED":
                    case "NET-ERR":
                    case "MEMEXEC":
                    case "FAILED":
                    case "SERIES-BLK":
                    case "EXPIRED":
                    case "REJECTED":
                    case "OUTPUT-REJ":
                    case "REJECTED-MULTIPART":
                    case "NO-DLR-OPTR":
                        return NotificationState.PERMANENT_FAILURE;
                    default:
                        //Fall through
                }
                return NotificationState.TEMPORARY_FAILURE;
            }

            @Override
            public NotificationState visit(StoredIVRNotificationItem ivr) {
                switch (status) {
                    case "ANSWER" :
                        return NotificationState.DELIVERED;
                    case "BUSY":
                    case "CANCEL":
                    case "CONGESTION":
                    case "NOANSWER":
                        return NotificationState.TEMPORARY_FAILURE;
                    case "FAILED":
                    default:
                        //Fall through
                }
                return NotificationState.TEMPORARY_FAILURE;
            }
        });

    }

    @SneakyThrows
    public static void toCsv(List<StoredNotificationItem> notificationItems, OutputStream outputStream) {
        val data = notificationItems.stream()
                .map(Utils::notificationToMap)
                .collect(Collectors.toList());
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema =CsvSchema.builder()
                .addColumn("ID", CsvSchema.ColumnType.STRING)
                .addColumn("Phone", CsvSchema.ColumnType.STRING)
                .addColumn("Provider Response", CsvSchema.ColumnType.STRING)
                .addColumn("Status", CsvSchema.ColumnType.STRING)
                .addColumn("Created", CsvSchema.ColumnType.STRING)
                .addColumn("Updated", CsvSchema.ColumnType.STRING)
                .setUseHeader(true)
                .build();
        try {
            mapper.writer(schema).writeValue(outputStream, data);
        } catch (Exception e) {
            log.error("Error processing json: ", e);
            throw e;
        }
    }

    public static String formatDate(Date date, ZoneId zone) {
            val dateTime = Instant.ofEpochMilli(date.getTime())
                    .atZone(zone)
                    .toLocalDateTime();
        return DATE_FORMATTER.format(dateTime);
    }

    private static Map<String, Object> notificationToMap(StoredNotificationItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("ID", item.getNotificationId());

        item.accept(new NotificationVisitor<Void>() {
            @Override
            public Void visit(StoredSmsNotificationItem sms) {
                map.put("Phone", sms.getPhone());
                return null;
            }

            @Override
            public Void visit(StoredIVRNotificationItem ivr) {
                map.put("Phone", ivr.getPhone());
                return null;
            }
        });

        map.put("Provider Response", item.getTranslatedProviderResponse());
        map.put("Status", item.getState().name());
        map.put("Created", formatDate(item.getCreated(), ZoneId.of("Asia/Calcutta")));
        map.put("Updated", formatDate(item.getUpdated(), ZoneId.of("Asia/Calcutta")));
        return map;
    }
}
