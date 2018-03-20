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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.PartnerDTO;

/**
 * Adds a {@link org.activityinfo.server.database.hibernate.entity.Partner} to
 * the the given
 * {@link org.activityinfo.server.database.hibernate.entity.UserDatabase}
 * <p/>
 * Returns {@link org.activityinfo.legacy.shared.command.result.VoidResult}
 *
 * @author Alex Bertram
 */
public class UpdatePartner implements MutatingCommand<CreateResult> {

    private int databaseId;
    private PartnerDTO partner;

    public UpdatePartner() {

    }

    public UpdatePartner(int databaseId, PartnerDTO partner) {
        this.databaseId = databaseId;
        this.partner = partner;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    public PartnerDTO getPartner() {
        return partner;
    }

    public void setPartner(PartnerDTO partner) {
        this.partner = partner;
    }
}