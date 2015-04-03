package org.activityinfo.test.capacity.load;

import org.activityinfo.test.capacity.action.ActionExecution;
import org.activityinfo.test.capacity.action.UserAction;
import org.activityinfo.test.capacity.model.ScenarioContext;
import org.activityinfo.test.capacity.model.UserRole;

import java.util.Iterator;

/**
 * A User's queue of actions to accomplish today
 */
public class UserQueue {
    private UserRole user;
    private Iterator<UserAction> tasks;

    public UserQueue(UserRole user, Iterator<UserAction> tasks) {
        this.user = user;
        this.tasks = tasks;
    }
    
    public Runnable next(ScenarioContext context) {
        return new ActionExecution(context, user, tasks.next());
    }
    
    public boolean hasNext() {
        return tasks.hasNext();
    }
    
}
