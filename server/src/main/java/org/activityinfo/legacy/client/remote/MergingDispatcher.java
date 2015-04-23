package org.activityinfo.legacy.client.remote;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandTimeoutException;
import org.activityinfo.legacy.shared.util.BackOff;
import org.activityinfo.legacy.shared.util.Commands;
import org.activityinfo.legacy.shared.util.ExponentialBackOff;
import org.activityinfo.server.endpoint.gwtrpc.AdvisoryLock;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link org.activityinfo.legacy.client.Dispatcher} that
 * merges equivalent commands independently executed during the same event loop.
 * <p/>
 * <p/>
 * This is critical when we have multiple, independent components that all
 * request GetSchema() or other basic information as they are loaded.
 */
public class MergingDispatcher extends AbstractDispatcher {

    private Dispatcher dispatcher;

    private Scheduler scheduler;

    /**
     * Pending commands have been requested but not yet sent to the server
     */
    private List<CommandRequest> pendingCommands = new ArrayList<CommandRequest>();

    /**
     * Executing commands have been sent to the server but for which we have not
     * yet received a response.
     */
    private List<CommandRequest> executingCommands = new ArrayList<CommandRequest>();

    private BackOff backOff;

    @Inject
    public MergingDispatcher(Dispatcher dispatcher, Scheduler scheduler) {
        this(dispatcher, scheduler, new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(AdvisoryLock.ADVISORY_GET_LOCK_TIMEOUT * 1000)
                .setMultiplier(2) // increase in 2 times
                .build());
    }

    public MergingDispatcher(Dispatcher dispatcher, Scheduler scheduler, BackOff backOff) {
        this.dispatcher = dispatcher;
        this.scheduler = scheduler;
        this.backOff = backOff;

        scheduler.scheduleFinally(new RepeatingCommand() {

            @Override
            public boolean execute() {
                try {
                    if (!pendingCommands.isEmpty()) {
                        dispatchPending();
                    }
                } catch (Exception e) {
                    Log.error("Uncaught exception while dispatching in MergingDispatcher", e);
                }
                return true;
            }
        });
    }

    @Override
    public <T extends CommandResult> void execute(Command<T> command, AsyncCallback<T> callback) {

        CommandRequest request = new CommandRequest(command, callback);

        if (Commands.hasMutatingCommand(command)) {
            // mutating requests get queued immediately, don't try to merge them
            // into any pending/executing commands, it wouldn't be correct

            queue(request);
        } else {
            if (!request.mergeSuccessfulInto(pendingCommands) && !request.mergeSuccessfulInto(executingCommands)) {

                queue(request);

                Log.debug("MergingDispatcher: Scheduled " + command.toString() + ", now " +
                        pendingCommands.size() + " command(s) pending");
            }
        }
    }

    private void queue(CommandRequest request) {
        pendingCommands.add(request);
    }

    private void dispatchPending() {
        Log.debug("MergingDispatcher: sending " + pendingCommands.size() + " to server.");

        final List<CommandRequest> sent = new ArrayList<CommandRequest>(pendingCommands);
        executingCommands.addAll(sent);
        pendingCommands.clear();

        if (!sent.isEmpty()) {
            for (final CommandRequest request : sent) {
                executeCommand(request, new RetryCountDown(backOff));
            }
        }
    }

    private void executeCommand(final CommandRequest request, final RetryCountDown retryCountDown) {
        dispatcher.execute(request.getCommand(), new AsyncCallback() {

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof CommandTimeoutException) {
                    Log.debug("Request timed out, retrying...");
                    retry(request, retryCountDown);
                } else {
                    executingCommands.remove(request);
                    request.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Object result) {
                executingCommands.remove(request);
                request.onSuccess(result);
            }
        });
    }

    private void retry(final CommandRequest request, final RetryCountDown retryCountDown) {
        try {
            long waitPeriod = retryCountDown.countDownAndGetWaitPeriod();
            Log.debug("Retry will run in " + waitPeriod + "ms.");
            scheduler.scheduleFixedPeriod(new RepeatingCommand() {
                @Override
                public boolean execute() {
                    executeCommand(request, retryCountDown);
                    return false;
                }
            }, (int) waitPeriod);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            executingCommands.remove(request);
            request.onFailure(e);
        }
    }
}
