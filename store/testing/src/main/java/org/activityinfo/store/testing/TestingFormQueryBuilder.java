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
package org.activityinfo.store.testing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.shared.GwtIncompatible;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.SubFormPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GwtIncompatible
public class TestingFormQueryBuilder implements ColumnQueryBuilder {

    private final List<TypedFormRecord> records;
    private final List<CursorObserver<ResourceId>> idObservers = new ArrayList<>();
    private final List<CursorObserver<FieldValue>> parentObservers = new ArrayList<>();
    private final Multimap<ResourceId, CursorObserver<FieldValue>> fieldObservers = HashMultimap.create();
    private final FormClass formClass;

    public TestingFormQueryBuilder(FormClass formClass, List<TypedFormRecord> records) {
        this.records = records;
        this.formClass = formClass;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {

        if(fieldId.equals(SubFormPatch.PERIOD_FIELD_ID)) {
            addResourceId(SubFormPatch.fromRecordId(formClass, observer));
            return;
        }

        if(fieldId.equals(FormClass.PARENT_FIELD_ID)) {
            if(!formClass.isSubForm()) {
                throw new IllegalStateException("Form " + formClass.getId() + " is not a sub form");
            }
            parentObservers.add(observer);
        } else {
            fieldObservers.put(fieldId, observer);
        }
    }

    @Override
    public void execute() {
        for (TypedFormRecord record : records) {
            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(record.getId());
            }
            for (CursorObserver<FieldValue> parentObserver : parentObservers) {
                parentObserver.onNext(new ReferenceValue(new RecordRef(formClass.getParentFormId().get(), record.getParentRecordId())));
            }
            for (Map.Entry<ResourceId, CursorObserver<FieldValue>> field : fieldObservers.entries()) {
                ResourceId fieldId = field.getKey();
                FieldValue fieldValue = record.get(fieldId);
                CursorObserver<FieldValue> observer = field.getValue();
                observer.onNext(fieldValue);
            }
        }

        for (CursorObserver<ResourceId> observer : idObservers) {
            observer.done();
        }
        for (CursorObserver<FieldValue> parentObserver : parentObservers) {
            parentObserver.done();
        }
        for (CursorObserver<FieldValue> observer : fieldObservers.values()) {
            observer.done();
        }
    }
}
