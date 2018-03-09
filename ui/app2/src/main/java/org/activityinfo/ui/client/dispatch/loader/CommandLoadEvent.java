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
package org.activityinfo.ui.client.dispatch.loader;

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.Loader;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;

/**
 * Subclass of GXT LoadEvent that allows listeners to piggy-back on a load
 * command. For instance, listeners could call addCommandToBatch() to add a save
 * command. Since the save command and the load command would be part of the
 * same batch, this would assure that if the save fails, the load command fails
 * as well.
 */
public class CommandLoadEvent extends LoadEvent {

    private BatchCommand batch = new BatchCommand();

    public CommandLoadEvent(Loader<?> loader) {
        super(loader);
    }

    public CommandLoadEvent(Loader<?> loader, Object config) {
        super(loader, config);
    }

    public CommandLoadEvent(Loader<?> loader, Object config, Object data) {
        super(loader, config, data);
    }

    public CommandLoadEvent(Loader<?> loader, Object config, Throwable t) {
        super(loader, config, t);
    }

    public BatchCommand getBatch() {
        return batch;
    }

    public void addCommandToBatch(Command command) {
        batch.add(command);
    }

}
