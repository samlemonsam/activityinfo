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
package org.activityinfo.legacy.shared.impl.pivot.bundler;

import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.reports.content.AttributeCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class AttributeBundler implements Bundler {
    private final Dimension dimension;
    private final String valueColumnAlias;
    private final String orderColumnAlias;

    public AttributeBundler(Dimension dimension, String valueColumnAlias, String orderColumnAlias) {
        super();
        this.dimension = dimension;
        this.valueColumnAlias = valueColumnAlias;
        this.orderColumnAlias = orderColumnAlias;
    }

    @Override
    public void bundle(SqlResultSetRow row, Bucket bucket) {
        if (!row.isNull(valueColumnAlias)) {
            bucket.setCategory(dimension,
                    new AttributeCategory(row.getString(valueColumnAlias), row.getInt(orderColumnAlias)));
        }
    }

}
