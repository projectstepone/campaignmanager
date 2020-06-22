package io.appform.campaignmanager.auth;

import io.appform.dropwizard.sharding.sharding.LookupKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.*;
import java.util.Date;

/**
 *
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_tokens", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_expiry", columnList = "expiry", unique = true)
})
@SQLDelete(sql = "UPDATE user_tokens set active='1' where id = ?", check = ResultCheckStyle.COUNT)
public class StoredUserToken {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private long id;

    @LookupKey
    @Column(nullable = false)
    private String token;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column
    private Date expiry;

    @Column
    private boolean deleted;

    @Column(name = "created", columnDefinition = "timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.INSERT)
    private Date created;

    @Column(name = "updated", columnDefinition = "timestamp default current_timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.ALWAYS)
    private Date updated;

    public StoredUserToken(String token, String userId, Date expiry) {
        this.token = token;
        this.userId = userId;
        this.expiry = expiry;
    }
}
