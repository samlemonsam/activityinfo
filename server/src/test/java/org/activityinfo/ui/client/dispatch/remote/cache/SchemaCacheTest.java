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
package org.activityinfo.ui.client.dispatch.remote.cache;

import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.DTOs;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.ui.client.MockEventBus;
import org.junit.Assert;
import org.junit.Test;

public class SchemaCacheTest {

    @Test
    public void testSchemaCache() {

        CacheManager proxyMgr = new CacheManager(new MockEventBus());

        new SchemaCache(proxyMgr);

        SchemaDTO schema = DTOs.pear();

        proxyMgr.notifyListenersOfSuccess(new GetSchema(), schema);

        CacheResult<SchemaDTO> proxyResult = proxyMgr.execute(new GetSchema());

        Assert
                .assertTrue("could execute locally", proxyResult.isCouldExecute());
        Assert.assertEquals("PEAR", proxyResult.getResult().getDatabaseById(1)
                .getName());
    }
}