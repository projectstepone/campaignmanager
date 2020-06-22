package io.appform.campaignmanager.okta;

import lombok.Builder;
import lombok.Data;
import lombok.val;

import java.io.Serializable;

/**
 *
 */
@Data
@Builder
public class SessionUser implements Serializable {
    private static final long serialVersionUID = -7917711435258380077L;

    private final User user;

    private static ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void put(User user) {
        currentUser.set(user);
    }

    public static User take() {
        val user = currentUser.get();
        currentUser.remove();
        return user;
    }

}
