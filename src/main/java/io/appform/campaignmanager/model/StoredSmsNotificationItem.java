package io.appform.campaignmanager.model;

import io.appform.campaignmanager.data.NotificationVisitor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@DiscriminatorValue("SMS")
public class StoredSmsNotificationItem extends StoredNotificationItem {

    @Column(nullable = false)
    private String phone;
    @Column(nullable = true)
    private String content;

    public StoredSmsNotificationItem() {
        super(NotificationType.SMS);
    }

    @Builder
    public StoredSmsNotificationItem(
            String notificationId,
            String campaignId,
            ProviderType provider,
            NotificationState state,
            String phone,
            String content) {
        super(NotificationType.SMS, notificationId, campaignId, provider, state);
        this.phone = phone;
        this.content = content;
    }

    @Override
    public <T> T accept(NotificationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
