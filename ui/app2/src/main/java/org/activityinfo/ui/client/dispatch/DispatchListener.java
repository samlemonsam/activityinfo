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
package org.activityinfo.ui.client.dispatch;

import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;

/**
 * Provides an interface through which caches can monitor the execution of
 * remote service calls.
 *
 * @param <T> The type of command for which to listen.
 */
public interface DispatchListener<T extends Command> {

    /**
     * Called just before action is taken to dispatch the given the Command.
     * Listeners can use this event to invalidate caches that would be affected
     * by mutating commands.
     */
    void beforeDispatched(T command);

    /**
     * Called following the successful dispatch of the given command.
     */
    void onSuccess(T command, CommandResult result);

    /**
     * Called following the failure, expected or otherwise, of the given command
     */
    public void onFailure(T command, Throwable caught);

}
