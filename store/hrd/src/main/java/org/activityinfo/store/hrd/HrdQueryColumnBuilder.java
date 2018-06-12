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

import com.google.common.collect.Lists;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.SubFormPatch;

import java.util.ArrayList;
import java.util.List;


class HrdQueryColumnBuilder implements ColumnQueryBuilder {

    private FormClass formClass;
    private List<CursorObserver<ResourceId>> idObservers = Lists.newArrayList();
    private List<FieldObserver> fieldObservers = Lists.newArrayList();
    private List<CursorObserver<FieldValue>> parentFieldObservers = null;
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

        FieldObserver fieldObserver;
        if(fieldId.equals(FormClass.PARENT_FIELD_ID)) {
            if (parentFieldObservers == null) {
                parentFieldObservers = new ArrayList<>();
            }
            parentFieldObservers.add(observer);
            observers.add(observer);

        } else if(fieldId.equals(SubFormPatch.PERIOD_FIELD_ID)) {
            addResourceId(SubFormPatch.fromRecordId(formClass, observer));

        } else {

            FormField field = formClass.getField(fieldId);
            FieldConverter converter = FieldConverters.forType(field.getType());
            fieldObserver = new FieldObserver(field.getName(), converter, observer);
            fieldObservers.add(fieldObserver);
            observers.add(observer);
        }
    }

    @Override
    public void execute() {

        Query<FormRecordEntity> query = Hrd.ofy()
                .load()
                .type(FormRecordEntity.class)
                .ancestor(FormEntity.key(formClass))
                .chunk(1000);

        for (FormRecordEntity entity : query.iterable()) {

            for (CursorObserver<ResourceId> idObserver : idObservers) {
                idObserver.onNext(entity.getRecordId());
            }
            for (FieldObserver fieldObserver : fieldObservers) {
                fieldObserver.onNext(entity.getFieldValues());
            }
            if(parentFieldObservers != null) {
                ResourceId parentRecordId = ResourceId.valueOf(entity.getParentRecordId());
                RecordRef parentRef = new RecordRef(formClass.getParentFormId().get(), parentRecordId);
                ReferenceValue parent = new ReferenceValue(parentRef);
                for (CursorObserver<FieldValue> parentFieldObserver : parentFieldObservers) {
                    parentFieldObserver.onNext(parent);
                }
            }
        }
        
        for (CursorObserver<?> observer : observers) {
            observer.done();
        }
    }
}
