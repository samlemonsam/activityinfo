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
package org.activityinfo.ui.client.store.offline;

import com.google.common.base.Function;
import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.promise.Maybe;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.Optional;

public class RecordQuery extends SimpleTask<Maybe<FormRecord>> {



    private final OfflineDatabase executor;
    private final RecordRef recordRef;

    public RecordQuery(OfflineDatabase executor, RecordRef recordRef) {
        this.recordRef = recordRef;
        this.executor = executor;
    }

    @Override
    protected Promise<Maybe<FormRecord>> execute() {
        return executor
            .begin(RecordStore.NAME)
            .query(tx -> {
                RecordStore recordStore = tx.objectStore(RecordStore.DEF);
                return recordStore.get(recordRef);
            })
            .then(new Function<Optional<RecordObject>, Maybe<FormRecord>>() {
                @Override
                public Maybe<FormRecord> apply(Optional<RecordObject> formRecord) {
                    if(formRecord.isPresent()) {
                        return Maybe.of(formRecord.get().toFormRecord(recordRef));
                    } else {
                        return Maybe.notFound();
                    }
                }
            });
    }
}
