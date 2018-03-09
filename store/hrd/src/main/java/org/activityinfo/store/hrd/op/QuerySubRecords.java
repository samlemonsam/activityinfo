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
package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.googlecode.objectify.Work;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class QuerySubRecords implements Work<List<FormRecord>> {

    private FormClass formClass;
    private ResourceId parentRecordId;

    public QuerySubRecords(FormClass formClass, ResourceId parentRecordId) {
        this.formClass = formClass;
        this.parentRecordId = parentRecordId;
    }

    @Override
    public List<FormRecord> run() {
        QueryResultIterable<FormRecordEntity> query = ofy().load()
                .type(FormRecordEntity.class)
                .ancestor(FormEntity.key(formClass))
                .filter("parentRecordId", this.parentRecordId.asString())
                .iterable();

        List<FormRecord> records = Lists.newArrayList();
        for (FormRecordEntity entity : query) {
            records.add(entity.toFormRecord(formClass));

        }
        return records;
    }
}
