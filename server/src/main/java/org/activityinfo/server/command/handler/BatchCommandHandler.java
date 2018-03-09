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
package org.activityinfo.server.command.handler;

import com.google.apphosting.api.ApiProxy;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BatchCommandHandler implements CommandHandler<BatchCommand> {

    private static final long BUFFER_MILLISECONDS = 10_000;
    
    private final DispatcherSync dispatcher;


    @Inject
    public BatchCommandHandler(DispatcherSync dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public CommandResult execute(BatchCommand cmd, User user) throws CommandException {

        List<CommandResult> results = new ArrayList<>(cmd.getCommands().size());
        
        for(Command command : cmd.getCommands()) {
            results.add(dispatcher.execute(command));
            
            if(timeRemaining() < BUFFER_MILLISECONDS) {
                throw new CommandException("No time remaining to complete batch");
            }
        }

        return new BatchResult(results);
    }

    private long timeRemaining() {
        if(ApiProxy.getCurrentEnvironment() == null) {
            return Long.MAX_VALUE;
        } else {
            return ApiProxy.getCurrentEnvironment().getRemainingMillis();
        }
    }
}
