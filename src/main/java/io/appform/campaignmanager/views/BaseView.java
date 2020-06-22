package io.appform.campaignmanager.views;

import io.dropwizard.views.View;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public abstract class BaseView extends View {
    private final String error;

    protected BaseView(String templateName, String error) {
        super(templateName);
        this.error = error;
    }
}
