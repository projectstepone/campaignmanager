package io.appform.campaignmanager.model;

import lombok.Getter;

/**
 * different notification providers supported
 */
public enum ProviderType {
    KALEYRA_SMS(NotificationType.SMS) {
        @Override
        public <T> T accept(ProviderTypeVisitor<T> visitor) {
            return visitor.visitKaleyraSms();
        }
    },
    KALEYRA_IVR(NotificationType.IVR) {
        @Override
        public <T> T accept(ProviderTypeVisitor<T> visitor) {
            return visitor.visitKaleyraIvr();
        }
    },
    ;

    @Getter
    private final NotificationType supportedNotificationType;

    ProviderType(NotificationType supportedNotificationType) {
        this.supportedNotificationType = supportedNotificationType;
    }

    public abstract <T> T accept(ProviderTypeVisitor<T> visitor);

    public interface ProviderTypeVisitor<T> {
        T visitKaleyraSms();
        T visitKaleyraIvr();
    }
}
