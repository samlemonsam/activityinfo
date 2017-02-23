package org.activityinfo.legacy.shared.impl;

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

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;

public interface CommandHandlerAsync<C extends Command<R>, R extends CommandResult> {

    /*
     * TODO: is there anyway the return type can be automatically parameratized
     * with the type parameter of CommandT ? (and without adding a second type
     * parameter to CommandHandler
     */

    /**
     * Execute a Command asynchronously
     *
     * @param <T>      Result type
     * @param command  Command to be executed
     * @param callback Callback to receive the command result or an exception
     */
    void execute(C command, ExecutionContext context, AsyncCallback<R> callback);

}
