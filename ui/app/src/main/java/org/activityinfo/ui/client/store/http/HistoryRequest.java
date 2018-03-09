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
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Promise;

import java.util.List;


public class HistoryRequest implements HttpRequest<RecordHistory> {
    private String formId;
    private String recordId;

    public HistoryRequest(RecordRef recordRef) {
        this.formId = recordRef.getFormId().asString();
        this.recordId = recordRef.getRecordId().asString();
    }

    @Override
    public Promise<RecordHistory> execute(ActivityInfoClientAsync client) {
        return client.getRecordHistory(formId, recordId);
    }

    @Override
    public int refreshInterval(RecordHistory result) {
        return -1;
    }
}
