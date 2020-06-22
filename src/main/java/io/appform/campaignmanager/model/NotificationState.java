package io.appform.campaignmanager.model;

/**
 * State of a paritcular notification
 */
public enum  NotificationState {
    CREATED,
    SENT,
    PERMANENT_FAILURE,
    TEMPORARY_FAILURE,
    DELIVERED,
}
