/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.component.importDialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromisesExecutionMonitor;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Submits records to persist serially to avoid swampping the server
 */
public class SerialQueue {

    private static final Logger LOGGER = Logger.getLogger(SerialQueue.class.getName());

    private static final int MAX_PENDING = 4;
    private static final int MAX_RETRIES = 3;
    private ResourceLocator locator;

    private class Task {
        private TypedFormRecord record;
        private Promise<Void> result;
        private int retries = 0;
    }

    private Promise<Void> result = new Promise<>();

    /**
     * Queue of FormInstances that need to be submitted
     */
    private List<Task> tasks = new ArrayList<>();

    private List<Task> pendingTasks = new ArrayList<>();


    private PromisesExecutionMonitor monitor;

    private AsyncCallback<Void> taskCallback;
    private boolean pollScheduled = false;
    private boolean polling = false;

    private int completed = 0;
    private int failed = 0;
    private int retries = 0;


    public SerialQueue(ResourceLocator locator, List<TypedFormRecord> toPersist, PromisesExecutionMonitor monitor) {
        this.locator = locator;

        for (TypedFormRecord typedFormRecord : toPersist) {
            Task task = new Task();
            task.record = typedFormRecord;
            tasks.add(task);
        }

        this.monitor = monitor;
        this.taskCallback = new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                schedulePoll();
            }

            @Override
            public void onSuccess(Void aVoid) {
                schedulePoll();
            }
        };
    }

    public Promise<Void> execute() {
        schedulePoll();
        return result;
    }

    private void pollQueue() {

        polling = true;

        while(pollScheduled) {

            LOGGER.log(Level.INFO, "Starting poll...");
            LOGGER.log(Level.INFO, "Pending tasks: " + pendingTasks.size());

            pollScheduled = false;

            // First check pending for any results
            Iterator<Task> pendingIt = new ArrayList<>(pendingTasks).iterator();
            while (pendingIt.hasNext()) {
                Task pending = pendingIt.next();
                if (pending.result.isSettled()) {
                    pendingTasks.remove(pending);
                    onSettled(pending);
                }
            }

            LOGGER.log(Level.INFO, "Pending tasks: " + pendingTasks.size());

            // If we have room in the queue, then submit additional
            // records to the server
            Iterator<Task> it = tasks.iterator();
            while (it.hasNext() && pendingTasks.size() < MAX_PENDING) {
                Task task = it.next();
                if (task.result == null) {
                    submit(task);
                }
            }

            LOGGER.log(Level.INFO, "Pending tasks: " + pendingTasks.size());


            // Are we done?
            if (pendingTasks.isEmpty()) {
                onDone();
            }
        }

        polling = false;
    }

    private void schedulePoll() {
        if(!pollScheduled) {
            pollScheduled = true;
            if(GWT.isClient()) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        pollQueue();
                    }
                });
            } else {
                if(!polling) {
                    pollQueue();
                }
            }
        }
    }

    private void submit(Task task) {
        task.result = locator.persist(task.record);
        task.result.then(taskCallback);
        pendingTasks.add(task);
    }


    private void onSettled(Task task) {
        if(task.result.getState() == Promise.State.REJECTED) {
            if(task.retries < MAX_RETRIES) {
                task.retries++;
                retries++;
                submit(task);
            } else {
                failed++;
            }
        } else {
            completed++;
        }
        if(monitor != null) {
            monitor.onChange(new PromisesExecutionMonitor.PromisesExecutionStatistic(completed, retries, tasks.size()));
        }
    }


    private void onDone() {
        LOGGER.log(Level.INFO, "All tasks complete: " + pendingTasks.size());

        result.onSuccess(null);
    }

}
