package io.appform.campaignmanager.handlebars;

import io.appform.campaignmanager.model.StoredNotificationItem;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 *
 */
@Slf4j
public class HandlebarsHelpers {

    private static final String STD_DATE_FORMAT = "dd-MMMMM-yyyy hh:mm:ss aaa";
    public CharSequence progressBarWidth(int total, int count) {
        if(total == 0) return "0%";
        return "" + ((double)count * 100 / total) + "%";
    }

    public CharSequence sampleText(final List<StoredNotificationItem> items) {
        if(null == items || items.isEmpty()) {
            return "";
        }
        return items.get(0).getContent();
    }
}
