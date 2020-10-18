package io.appform.campaignmanager.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.appform.campaignmanager.data.NotificationVisitor;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.util.Date;

/**
 *
 */
@Data
@Entity
@Table(
        name = "notification_items",
        indexes = {
                @Index(name = "idx_campaign_state", columnList = "campaign_id, state")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "SMS", value = StoredSmsNotificationItem.class)

})
public abstract class StoredNotificationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", insertable = false, updatable = false)
    private final NotificationType type;

    @Column(name = "notification_id", nullable = false, unique = true)
    private String notificationId;

    @Column(name = "content", nullable = true, columnDefinition = "blob")
    private String content;

    @Column(name = "campaign_id", nullable = false)
    private String campaignId;

    @Enumerated(EnumType.STRING)
    @Column
    private ProviderType provider;

    @Column(name = "http_status")
    private int httpStatus;

    @Column(name = "http_response", columnDefinition = "blob")
    private String httpResponse;

    @Column(name = "translated_provider_response", columnDefinition = "blob")
    private String translatedProviderResponse;

    @Enumerated(EnumType.STRING)
    @Column
    private NotificationState state;

    @Column(name = "created", columnDefinition = "timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.INSERT)
    private Date created;

    @Column(name = "updated", columnDefinition = "timestamp default current_timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.ALWAYS)
    private Date updated;


    protected StoredNotificationItem(NotificationType type) {
        this.type = type;
    }

    protected StoredNotificationItem(
            NotificationType type,
            String notificationId,
            String campaignId,
            ProviderType provider,
            NotificationState state) {
        this(type);
        this.notificationId = notificationId;
        this.campaignId = campaignId;
        this.provider = provider;
        this.state = state;
    }

    public abstract <T> T accept(NotificationVisitor<T> visitor);
}
