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

import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;
import org.activityinfo.store.spi.RecordVersion;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class QueryVersions implements Work<List<RecordVersion>> {

    private FormClass formClass;
    private ResourceId recordId;
    private ResourceId parentRecordId;

    public QueryVersions(FormClass formClass) {
        this.formClass = formClass;
    }

    public static QueryVersions of(FormClass formClass, ResourceId recordId) {
        QueryVersions queryVersions = new QueryVersions(formClass);
        queryVersions.recordId = recordId;
        return queryVersions;
    }
    
    public static QueryVersions subRecords(FormClass formClass, ResourceId parentRecordId) {
        QueryVersions queryVersions = new QueryVersions(formClass);
        queryVersions.parentRecordId = parentRecordId;
        return queryVersions;
    }
    
    @Override
    public List<RecordVersion> run() {

        Query<FormRecordSnapshotEntity> query;

        if(recordId != null) {
            Key<FormRecordEntity> recordKey = FormRecordEntity.key(formClass, recordId);
            query = ofy().load()
                    .type(FormRecordSnapshotEntity.class)
                    .ancestor(recordKey);

        } else {
            Key<FormEntity> rootKey = FormEntity.key(formClass);
            query = ofy().load()
                    .type(FormRecordSnapshotEntity.class)
                    .ancestor(rootKey)
                    .filter("parentRecordId", parentRecordId.asString());
        }

        List<RecordVersion> versions = new ArrayList<>();
        
        for (FormRecordSnapshotEntity snapshot : query.iterable()) {
            RecordVersion version = new RecordVersion();
            version.setRecordId(snapshot.getRecordId());
            version.setVersion(snapshot.getVersion());
            version.setUserId(snapshot.getUserId());
            version.setTime(snapshot.getTime().getTime());
            version.setType(snapshot.getType());

            if (formClass.isSubForm()) {
                version.setSubformKind(formClass.getSubFormKind());
                version.setSubformKey(subformKey(snapshot));
            }

            version.getValues().putAll(snapshot.getRecord().toFieldValueMap(formClass));
            versions.add(version);
        }
        return versions;
    }

    private String subformKey(FormRecordSnapshotEntity snapshot) {
        if (formClass.getSubFormKind() == SubFormKind.REPEATING) {
            return "";
        } else {
            return getPeriodKey(snapshot);
        }
    }

    private String getPeriodKey(FormRecordSnapshotEntity snapshot) {
        Optional<String> periodKey = periodKeyFromPeriodField(snapshot);
        if (periodKey.isPresent()) {
            return periodKey.get();
        } else {
            return periodKeyFromRecordId(snapshot.getRecordId());
        }
    }

    private Optional<String> periodKeyFromPeriodField(FormRecordSnapshotEntity snapshot) {
        Optional<FormField> periodField = formClass.getFieldIfPresent(ResourceId.valueOf("period"));
        if (periodField.isPresent()) {
            FieldConverter converter = FieldConverters.forType(periodField.get().getType());
            Object period = snapshot.getRecord().getFieldValues().getProperty(periodField.get().getName());
            FieldValue periodValue = converter.toFieldValue(period);
            return periodValue != null ? Optional.of(periodValue.toString()) : Optional.absent();
        } else {
            return Optional.absent();
        }
    }

    private String periodKeyFromRecordId(ResourceId subFormRecordId) {
        RecordRef ref = new RecordRef(formClass.getId(), subFormRecordId);
        PeriodType periodType = formClass.getSubFormKind().getPeriodType();
        return periodType.fromSubFormKey(ref).toString();
    }

}
