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
package org.activityinfo.ui.client.offline.ui;

import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.inject.Inject;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.offline.command.OutOfSyncStatus;

public class SyncStatusBar extends ToolBar {

    public static final int HEIGHT = 30;

    @Inject
    public SyncStatusBar(EventBus eventBus, OutOfSyncStatus outOfSyncStatus, CommandQueueStatus commandQueueStatus) {
        setHeight(HEIGHT);
        add(new WorkStatus(eventBus));
        add(new LastSyncStatus(eventBus));
        add(outOfSyncStatus);
        add(commandQueueStatus);

        SyncStatusResources.INSTANCE.style().ensureInjected();
    }
}
