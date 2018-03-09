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
package org.activityinfo.test.api;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;

/**
 * An ActivityInfo database created on the server for testing
 */
public class TestDatabase {

    private int id;
    private String name;
    private ResourceId firstPartnerId;

    public TestDatabase(ResourceId resourceId, String name, ResourceId firstPartnerId) {
        this.name = name;
        this.id = CuidAdapter.getLegacyIdFromCuid(resourceId);

        this.firstPartnerId = firstPartnerId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResourceId getPartnerFormId() {
        return CuidAdapter.partnerFormId(id);
    }

    public ReferenceValue getDefaultPartner() {
        return new ReferenceValue(new RecordRef(getPartnerFormId(), firstPartnerId));
    }
}
