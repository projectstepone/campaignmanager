package io.appform.campaignmanager.data;

import io.appform.campaignmanager.model.StoredIVRNotificationItem;
import io.appform.campaignmanager.model.StoredSmsNotificationItem;

/**
 *
 */
public interface NotificationVisitor<T> {
    T visit(StoredSmsNotificationItem sms);

    T visit(StoredIVRNotificationItem ivr);
}
