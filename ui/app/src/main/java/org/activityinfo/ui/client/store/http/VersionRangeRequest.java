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
import org.activityinfo.model.form.FormSyncSet;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * Requests a range of record versions for a given form.
 */
public class VersionRangeRequest implements HttpRequest<FormSyncSet> {

    private ResourceId formId;
    private long localVersion;
    private long version;

    public VersionRangeRequest(ResourceId formId, long localVersion, long version) {
        this.formId = formId;
        this.localVersion = localVersion;
        this.version = version;
    }

    @Override
    public Promise<FormSyncSet> execute(ActivityInfoClientAsync client) {
        return client.getRecordVersionRange(formId.asString(), localVersion, version);
    }


    @Override
    public int refreshInterval(FormSyncSet result) {
        return -1;
    }
}
