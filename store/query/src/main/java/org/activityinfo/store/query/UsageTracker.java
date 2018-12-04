package org.activityinfo.store.query;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.logging.Logger;

/**
 * Tracks statistics on which parts of the application are used
 */
public class UsageTracker {

    private static final Logger LOGGER = Logger.getLogger(UsageTracker.class.getName());


    public static void track(int userId, String action) {
        track(userId, action, null, null);
    }

    public static void track(int userId, String action, ResourceId databaseId) {
        track(userId, action, databaseId, null);
    }

    public static void track(int userId, String action, FormClass formSchema) {
        track(userId, action, formSchema.getDatabaseId(), formSchema.getId());
    }

    public static void track(int userId, String action, ResourceId databaseId, ResourceId formId) {
        StringBuilder message = new StringBuilder("TRACK: User ");
        message.append(userId);
        message.append(' ');
        message.append(action);
        if(databaseId != null) {
            message.append(", databaseId:");
            message.append(databaseId.asString());
        }
        if(formId != null) {
            message.append(", formId:");
            message.append(formId.asString());
        }
        LOGGER.info(message.toString());
    }

}
