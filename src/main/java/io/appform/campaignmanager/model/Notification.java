package io.appform.campaignmanager.model;

import lombok.Value;

/**
 * The <code>Notification</code> value object.
 */
@Value
public class Notification {

    /**
     * The phone no.
     */
    String phone;
    
    /**
     * The content.
     */
    String content; 
}
