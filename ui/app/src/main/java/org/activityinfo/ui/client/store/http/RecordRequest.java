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
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.FormChange;

public class RecordRequest implements HttpRequest<Maybe<FormRecord>> {

    private RecordRef recordRef;

    public RecordRequest(RecordRef recordRef) {
        this.recordRef = recordRef;
    }

    @Override
    public Promise<Maybe<FormRecord>> execute(ActivityInfoClientAsync client) {
        return client.getRecord(recordRef.getFormId().asString(), recordRef.getRecordId().asString());
    }

    @Override
    public int refreshInterval(Maybe<FormRecord> result) {
        return -1;
    }
}
