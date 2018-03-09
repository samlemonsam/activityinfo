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

import org.activityinfo.indexedb.OfflineDatabase;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.form.FormPermissions;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.store.tasks.SimpleTask;

import java.util.logging.Logger;


public class SchemaQuery extends SimpleTask<FormClass> {

    private static final Logger LOGGER = Logger.getLogger(SchemaQuery.class.getName());

    private ResourceId formId;
    private OfflineDatabase executor;

    public SchemaQuery(OfflineDatabase executor, ResourceId formId) {
        this.formId = formId;
        this.executor = executor;
    }

    @Override
    protected Promise<FormClass> execute() {
        return this.executor.begin(SchemaStore.DEF)
        .query(tx -> tx.objectStore(SchemaStore.DEF).get(formId))
        .then(formClass -> {
            if (formClass.isPresent()) {
                return formClass.get();
            } else {
                throw new IllegalStateException("FormSchema entry is missing for " + formId);
            }
        });
    }

    @Override
    public int refreshInterval(FormClass result) {
        return 0;
    }
}
