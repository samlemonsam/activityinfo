package org.activityinfo.server.event;

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

import com.extjs.gxt.ui.client.data.RpcMap;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.SiteCommand;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.model.auth.AuthenticatedUser;

@SuppressWarnings("rawtypes")
public class CommandEvent {
    private Command command;
    private CommandResult result;
    private ExecutionContext context;

    public CommandEvent(Command command, CommandResult result, ExecutionContext context) {
        this.command = command;
        this.result = result;
        this.context = context;
    }

    public Command getCommand() {
        return this.command;
    }

    public CommandResult getResult() {
        return result;
    }

    public ExecutionContext getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "CommandEvent [" + getCommand().getClass().getSimpleName() + "]";
    }

    public Integer getUserId() {
        AuthenticatedUser au = getContext().getUser();
        if (au != null) {
            return au.getUserId();
        }
        return null;
    }

    public Integer getSiteId() {
        if (getCommand() instanceof SiteCommand) {
            return ((SiteCommand) getCommand()).getSiteId();
        }
        return null;
    }

    public RpcMap getRpcMap() {
        if (getCommand() instanceof SiteCommand) {
            return ((SiteCommand) getCommand()).getProperties();
        }
        return null;
    }
}
