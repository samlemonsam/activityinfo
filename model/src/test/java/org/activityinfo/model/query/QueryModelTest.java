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
package org.activityinfo.model.query;

import org.activityinfo.json.Json;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

public class QueryModelTest {

    @Test
    public void clientSideSerialization() {
        QueryModel model = new QueryModel(ResourceId.valueOf("XYZ"));
        model.selectResourceId().as("id");
        model.selectResourceId();
        model.selectExpr("foo").as("foo_column");

        String json = model.toJsonString();

        System.out.println(json);

        QueryModel remodel = QueryModel.fromJson(Json.parse(json));
    }

}