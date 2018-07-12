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
package org.activityinfo.store.hrd;

import com.google.appengine.api.datastore.*;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.SubFormPatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


class HrdQueryColumnBuilder implements ColumnQueryBuilder {

    private static final int BYTES_PER_MEGABYTE = 1024 * 1024;
    private static final Logger LOGGER = Logger.getLogger(HrdQueryColumnBuilder.class.getName());

    private FormClass formClass;
    private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
    private List<FieldObserver> fieldObservers = Lists.newArrayList();
    private List<CursorObserver<FieldValue>> parentFieldObservers = null;
    private List<PeriodObserver> periodObservers = new ArrayList<>();
    private List<CursorObserver<?>> observers = Lists.newArrayList();

    HrdQueryColumnBuilder(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
        observers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {

        if(fieldId.equals(FormClass.PARENT_FIELD_ID)) {
            if (parentFieldObservers == null) {
                parentFieldObservers = new ArrayList<>();
            }
            parentFieldObservers.add(observer);
            observers.add(observer);

        } else if(fieldId.equals(SubFormPatch.PERIOD_FIELD_ID)) {
            PeriodObserver periodObserver = new PeriodObserver(formClass, observer);
            if (periodObservers == null) {
                periodObservers = new ArrayList<>();
            }
            periodObservers.add(periodObserver);
            observers.add(periodObserver);

        } else {
            FieldObserver fieldObserver;
            FormField field = formClass.getField(fieldId);
            FieldConverter converter = FieldConverters.forType(field.getType());
            fieldObserver = new FieldObserver(field.getName(), converter, observer);
            fieldObservers.add(fieldObserver);
            observers.add(observer);
        }
    }

    @Override
    public void execute() {

        // This method is performance and memory critical.
        // For large forms, (~40k records), it is easy to hit an OutOfMemory exception or
        // exceed the request time limit.

        // For this reason, we use the raw datastore API rather than the Objectify API which
        // adds a layer of abstraction and additional memory allocation.

        LOGGER.info("Starting query: " + megabytesFree() + "mb");

        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery preparedQuery = datastoreService.prepare(
                new com.google.appengine.api.datastore.Query("FormRecord")
                    .setAncestor(FormEntity.key(formClass).getRaw()));

        FetchOptions fetchOptions = FetchOptions.Builder.withChunkSize(500).prefetchSize(500);
        Iterator<Entity> it = preparedQuery.asIterator(fetchOptions);

        while(it.hasNext()) {
            Entity entity = it.next();
            ResourceId recordId = ResourceId.valueOf(entity.getKey().getName());

            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(recordId);
            }

            EmbeddedEntity fieldValues = (EmbeddedEntity) entity.getProperty("fieldValues");
            for (FieldObserver fieldObserver : fieldObservers) {
                fieldObserver.onNext(fieldValues);
            }
            if(periodObservers != null) {
                for (PeriodObserver periodObserver : periodObservers) {
                    periodObserver.onNext(entity);
                }
            }
            if(parentFieldObservers != null) {
                String parentRecordId = (String)entity.getProperty("parentRecordId");
                RecordRef parentRef = new RecordRef(formClass.getParentFormId().get(), ResourceId.valueOf(parentRecordId));
                ReferenceValue parent = new ReferenceValue(parentRef);
                for (CursorObserver<FieldValue> parentFieldObserver : parentFieldObservers) {
                    parentFieldObserver.onNext(parent);
                }
            }
        }

        for (CursorObserver<?> observer : observers) {
            observer.done();
        }

        LOGGER.info("Finished query: " + megabytesFree() + "mb");
    }

    private long megabytesFree() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MEGABYTE;
    }
}
