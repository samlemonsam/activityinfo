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

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AnalysisEntity;

import java.util.Collections;

public class ReportFolder {

    public static Iterable<CatalogEntry> queryReports(String parentId) {

        if(parentId.charAt(0) != CuidAdapter.DATABASE_DOMAIN) {
            return Collections.emptyList();
        }

        QueryResultIterable<AnalysisEntity> result = Hrd.ofy().load()
            .type(AnalysisEntity.class)
            .filter("parentId", parentId)
            .iterable();

        return Iterables.transform(result, new Function<AnalysisEntity, CatalogEntry>() {
            @Override
            public CatalogEntry apply(AnalysisEntity analysisEntity) {
                return new CatalogEntry(analysisEntity.getId(), analysisEntity.getLabel(), CatalogEntryType.ANALYSIS);
            }
        });
    }
}
