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
package org.activityinfo.server.endpoint.export;

import com.google.common.base.Preconditions;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.generated.StorageProvider;


public class TaskContext implements DispatcherSync {
    
    private final DispatcherSync dispatcherSync;
    private final StorageProvider storageProvider;
    private final String taskId;
    private final String rootUri;

    public TaskContext(DispatcherSync dispatcherSync, StorageProvider storageProvider, String taskId) {
       this(dispatcherSync, storageProvider, taskId, "https://www.activityinfo.org");
    }

    public TaskContext(DispatcherSync dispatcherSync, StorageProvider storageProvider, String taskId, String rootUri) {
        this.dispatcherSync = dispatcherSync;
        this.storageProvider = storageProvider;
        this.taskId = taskId;
        this.rootUri = rootUri;
    }

    public String getRootUri() {
        return rootUri;
    }

    public void updateProgress(double percentComplete) {
        Preconditions.checkArgument(percentComplete >= 0 && percentComplete <= 1.0d, 
                "percentComplete must be in the range [0, 1], was: %f", percentComplete);
    
        storageProvider.get(taskId).updateProgress(percentComplete);
    }

    @Override
    public <C extends Command<R>, R extends CommandResult> R execute(C command) throws CommandException {
        return dispatcherSync.execute(command);
    }
}
