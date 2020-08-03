package io.appform.campaignmanager.model;

import io.appform.dropwizard.sharding.sharding.LookupKey;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents an overall campaign
 */
@Data
@Entity
@Table(name = "campaigns")
@NoArgsConstructor
public class StoredCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @LookupKey
    @Column(name = "campaign_id", unique = true, nullable = false)
    private String campaignId;

    @Column(unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

    @Column(name = "content", nullable = false, columnDefinition = "blob")
    private String content;

    @Column(name = "send_as", nullable = false, columnDefinition = "blob")
    private String sendAs;

    @Column(name = "auth_key")
    private String authKey;

    @Column(name = "item_count")
    private long itemCount;

    @Column
    private long completed;

    @Column
    private long sent;


    @Column
    private long failures;

    @Column(name = "permanent_failures")
    private long permanentFailures;

    @Column(name = "temporary_failures")
    private long temporaryFailures;

    @Column
    private long delivered;

    @Column(name = "delivery_failures")
    private long deliveryFailures;

    @Enumerated(EnumType.STRING)
    @Column
    private CampaignState state;

    @Column(name = "created", columnDefinition = "timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.INSERT)
    private Date created;

    @Column(name = "updated", columnDefinition = "timestamp default current_timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.ALWAYS)
    private Date updated;

    @Builder
    public StoredCampaign(
            String campaignId,
            String name,
            String createdBy,
            NotificationType notificationType,
            String content,
            String sendAs,
            String authKey,
            CampaignState state,
            long itemCount) {
        this.campaignId = campaignId;
        this.name = name;
        this.createdBy = createdBy;
        this.notificationType = notificationType;
        this.content = content;
        this.sendAs = sendAs;
        this.authKey = authKey;
        this.state = state;
        this.itemCount = itemCount;
    }
}
