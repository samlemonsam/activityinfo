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
package org.activityinfo.server.event.sitehistory;

import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.CreateSite;
import org.activityinfo.legacy.shared.command.DeleteSite;
import org.activityinfo.legacy.shared.command.UpdateSite;
import org.activityinfo.server.event.CommandEvent;

public enum ChangeType {
    CREATE,
    UPDATE,
    DELETE,
    UNKNOWN;

    public static ChangeType getType(CommandEvent event) {
        return ChangeType.getType(event.getCommand());
    }

    @SuppressWarnings("rawtypes")
    public static ChangeType getType(Command cmd) {
        if (cmd instanceof CreateSite) {
            return CREATE;
        } else if (cmd instanceof UpdateSite) {
            return UPDATE;
        } else if (cmd instanceof DeleteSite) {
            return DELETE;
        } else {
            return UNKNOWN;
        }
    }

    public boolean isKnown() {
        return (this != UNKNOWN);
    }

    public boolean isNew() {
        return (this == CREATE);
    }

    public boolean isNewOrUpdate() {
        return (this == CREATE || this == UPDATE);
    }

    public boolean isDelete() {
        return (this == DELETE);
    }
}
