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
package org.activityinfo.store.mysql;

import org.activityinfo.json.Json;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.RecordRef;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class MySqlHistoryTest extends AbstractMySqlTest {


    @Before
    public void setupDatabase() throws Throwable {
        resetDatabase("history.db.xml");
    }

    @Test
    public void locationChange() throws SQLException {
        MySqlRecordHistoryBuilder builder = new MySqlRecordHistoryBuilder(catalog);
        RecordHistory array = builder.build(new RecordRef(
                CuidAdapter.activityFormClass(33),
                CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, 968196924)));

        System.out.println(Json.stringify(Json.toJson(array), 2));
    }

}
