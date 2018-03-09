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
package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.List;

public class SubRecordsRequest implements HttpRequest<List<FormRecord>> {
    private String formId;
    private String parentId;

    public SubRecordsRequest(ResourceId formId, RecordRef parent) {
        this.formId = formId.asString();
        this.parentId = parent.getRecordId().asString();
    }

    @Override
    public Promise<List<FormRecord>> execute(ActivityInfoClientAsync client) {
        return client.getRecords(formId, parentId).then(set -> set.getRecords());
    }

    @Override
    public int refreshInterval(List<FormRecord> result) {
        return 0;
    }
}
