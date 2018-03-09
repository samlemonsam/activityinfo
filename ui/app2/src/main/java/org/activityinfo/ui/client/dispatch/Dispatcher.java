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
/**
 * Supports the execution of {@link org.activityinfo.legacy.shared.command.Command},
 * either remotely or through local proxies/caches.
 */
package org.activityinfo.ui.client.dispatch;

import com.google.common.base.Function;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.promise.Promise;

/**
 * Executes {@link org.activityinfo.legacy.shared.command.Command}s on behalf of client
 * code. The implementation handles authentication with the server, batching
 * commands, caching, and everything else complicated.
 *
 * @author Alex Bertram (akbertram@gmail.com)
 */
public interface Dispatcher extends Function<Command, Promise<CommandResult>> {

    /**
     * Attempts to execute a command. The command may not neccessarily be
     * executed immediately, and there are no gaurantees that commands will
     * complete in the order they were submitted.
     *
     * @param command  The command to execute
     * @param monitor  The monitor which handles status reports to the user
     * @param callback The callback which implements application logic
     * @param <T>      The type of the {@code CommandResult}
     */
    <T extends CommandResult> void execute(Command<T> command, AsyncMonitor monitor, AsyncCallback<T> callback);

    /**
     * Attempts to execute a command. The command may not neccessarily be
     * executed immediately, and there are no gaurantees that commands will
     * complete in the order they were submitted.
     *
     * @param command  The command to execute
     * @param callback The callback which implements application logic
     * @param <T>      The type of the {@code CommandResult}
     */
    <T extends CommandResult> void execute(Command<T> command, AsyncCallback<T> callback);


    /**
     * Returns a retryable Promise for the given {@code Command}
     *
     * @param command
     * @param <T>
     * @return
     */
    <T extends CommandResult> Promise<T> execute(Command<T> command);
    
    
}
