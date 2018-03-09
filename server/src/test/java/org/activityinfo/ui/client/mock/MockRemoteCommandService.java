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
package org.activityinfo.ui.client.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.RemoteCommandServiceAsync;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.SchemaDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Bertram (akbertram@gmail.com)
 */
public class MockRemoteCommandService implements RemoteCommandServiceAsync {

    public Map<Class, Integer> commandCounts = new HashMap<Class, Integer>();

    public SchemaDTO schema;

    public MockRemoteCommandService() {

    }

    public MockRemoteCommandService(SchemaDTO schema) {
        this.schema = schema;
    }

    public int getCommandCount(Class clazz) {
        Integer count = commandCounts.get(clazz);
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    @Override
    public void execute(String authToken, String locale, List<Command> cmds,
                        AsyncCallback<List<CommandResult>> callback) {

        List<CommandResult> results = new ArrayList<CommandResult>();

        for (Command cmd : cmds) {

            Integer count = commandCounts.get(cmd.getClass());
            commandCounts.put(cmd.getClass(), count == null ? 1 : count + 1);

            if (schema != null && cmd instanceof GetSchema) {
                results.add(schema);
            } else {
                results.add(mockExecute(cmd));
            }
        }
        callback.onSuccess(results);
    }

    protected CommandResult mockExecute(Command cmd) {
        return new CommandException();
    }
}
