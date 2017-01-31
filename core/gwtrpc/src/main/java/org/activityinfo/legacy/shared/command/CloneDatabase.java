package org.activityinfo.legacy.shared.command;
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

import org.activityinfo.legacy.shared.command.result.CreateResult;

/**
 * @author yuriyz on 11/14/2014.
 */
public class CloneDatabase implements MutatingCommand<CreateResult> {

    private int sourceDatabaseId;
    private String name;
    private String description;
    private int countryId;
    private boolean copyData; // copies FormInstances as well as FormClasses
    private boolean copyPartners;
    private boolean copyUserPermissions; // copies UserPermissions (implies copyPartners and requires Design privileges on source database)

    public CloneDatabase() {
    }

    public String getName() {
        return name;
    }

    public CloneDatabase setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CloneDatabase setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getSourceDatabaseId() {
        return sourceDatabaseId;
    }

    public CloneDatabase setSourceDatabaseId(int sourceDatabaseId) {
        this.sourceDatabaseId = sourceDatabaseId;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public CloneDatabase setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public boolean isCopyData() {
        return copyData;
    }

    public CloneDatabase setCopyData(boolean copyData) {
        this.copyData = copyData;
        return this;
    }

    public boolean isCopyPartners() {
        return copyPartners;
    }

    public CloneDatabase setCopyPartners(boolean copyPartners) {
        this.copyPartners = copyPartners;
        return this;
    }

    public boolean isCopyUserPermissions() {
        return copyUserPermissions;
    }

    public CloneDatabase setCopyUserPermissions(boolean copyUserPermissions) {
        this.copyUserPermissions = copyUserPermissions;
        return this;
    }
}
