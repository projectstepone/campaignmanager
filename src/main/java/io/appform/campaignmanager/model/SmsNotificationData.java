package io.appform.campaignmanager.model;

import lombok.Value;

/**
 * The <code>SmsNotificationData</code> value object.
 */
@Value
public class SmsNotificationData {

    String phone;
    
    String content; 
}
