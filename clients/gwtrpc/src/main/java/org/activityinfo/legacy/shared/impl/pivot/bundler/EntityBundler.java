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
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.Dimension;

public class EntityBundler implements Bundler {
    private final Dimension dimension;
    private final String idAlias;
    private final String labelAlias;

    public EntityBundler(Dimension key, String idAlias, String labelAlias) {
        this.dimension = key;
        this.idAlias = idAlias;
        this.labelAlias = labelAlias;
    }

    @Override
    public void bundle(SqlResultSetRow row, Bucket bucket) {
        if (!row.isNull(idAlias)) {
            bucket.setCategory(dimension, new EntityCategory(row.getInt(idAlias), row.getString(labelAlias)));
        }
    }

}