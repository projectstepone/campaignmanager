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
import java.util.Set;

/**
 *
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE teams set deleted='1' where id = ?", check = ResultCheckStyle.COUNT)
public class StoredUser {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private long id;

    @LookupKey
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "roles", columnDefinition = "blob")
    @Convert(converter = RoleSetConverter.class)
    private Set<String> roles;

    @Column
    private boolean deleted;

    @Column(name = "created", columnDefinition = "timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.INSERT)
    private Date created;

    @Column(name = "updated", columnDefinition = "timestamp default current_timestamp", updatable = false, insertable = false)
    @Generated(value = GenerationTime.ALWAYS)
    private Date updated;


    public StoredUser(String userId, Set<String> roles) {
        this.userId = userId;
        this.roles = roles;
    }
}
