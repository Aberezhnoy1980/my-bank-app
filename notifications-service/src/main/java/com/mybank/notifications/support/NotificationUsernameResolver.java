package com.mybank.notifications.support;

import com.mybank.notification.events.NotificationEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NotificationUsernameResolver {

    private static final Pattern FOR_USER = Pattern.compile("for ([^\\s]+)");
    private static final Pattern FROM_USER = Pattern.compile("from ([^\\s]+) to");

    private NotificationUsernameResolver() {
    }

    public static String resolve(NotificationEvent event) {
        if (event == null || event.message() == null) {
            return "unknown";
        }
        return resolve(event.message());
    }

    public static String resolve(String message) {
        Matcher forUser = FOR_USER.matcher(message);
        if (forUser.find()) {
            return forUser.group(1);
        }
        Matcher fromUser = FROM_USER.matcher(message);
        if (fromUser.find()) {
            return fromUser.group(1);
        }
        return "unknown";
    }
}
