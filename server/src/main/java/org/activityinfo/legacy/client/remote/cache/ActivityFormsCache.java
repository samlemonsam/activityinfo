package org.activityinfo.legacy.client.remote.cache;

import org.activityinfo.legacy.client.CommandCache;
import org.activityinfo.legacy.client.DispatchListener;
import org.activityinfo.legacy.shared.command.GetActivityForms;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.command.result.CommandResult;

/**
 * An indicator's activity never changes so we can always cache the result
 */
public class ActivityFormsCache implements DispatchListener<GetActivityForms>, CommandCache<GetActivityForms> {
    
    @Override
    public void beforeDispatched(GetActivityForms command) {
    }

    @Override
    public void onSuccess(GetActivityForms command, CommandResult commandResult) {
        ActivityFormResults results = (ActivityFormResults) commandResult;
    }

    @Override
    public void onFailure(GetActivityForms command, Throwable caught) {

    }

    @Override
    public CacheResult maybeExecute(GetActivityForms command) {
        return null;
    }

    @Override
    public void clear() {

    }
}
