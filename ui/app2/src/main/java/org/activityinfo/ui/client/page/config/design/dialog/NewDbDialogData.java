package org.activityinfo.ui.client.page.config.design.dialog;
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

import org.activityinfo.legacy.shared.command.CloneDatabase;

/**
 * @author yuriyz on 11/14/2014.
 */
public class NewDbDialogData {

    private int sourceDatabaseCountryId = -1;
    private boolean hasDesignPrivileges = false;
    private CloneDatabase command = new CloneDatabase();
    private boolean cloneDb = false;

    public NewDbDialogData() {
    }

    public int getSourceDatabaseCountryId() {
        return sourceDatabaseCountryId;
    }

    public NewDbDialogData setSourceDatabaseCountryId(int sourceDatabaseCountryId) {
        this.sourceDatabaseCountryId = sourceDatabaseCountryId;
        return this;
    }

    public CloneDatabase getCommand() {
        return command;
    }

    public boolean hasDesignPrivileges() {
        return hasDesignPrivileges;
    }

    public NewDbDialogData setHasDesignPrivileges(boolean hasDesignPrivileges) {
        this.hasDesignPrivileges = hasDesignPrivileges;
        return this;
    }

    public boolean isCloneDb() {
        return cloneDb;
    }

    public NewDbDialogData setCloneDb(boolean cloneDb) {
        this.cloneDb = cloneDb;
        return this;
    }
}
