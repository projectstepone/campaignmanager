package io.appform.campaignmanager.views;

import io.appform.campaignmanager.model.StoredNotificationItem;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 *
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemsSnapshotView extends BaseView {
    private final List<StoredNotificationItem> items;

    @Builder
    public ItemsSnapshotView(
            List<StoredNotificationItem> items,
            String error) {
        super("itemssnapshot.hbs", error);
        this.items = items;
    }
}
