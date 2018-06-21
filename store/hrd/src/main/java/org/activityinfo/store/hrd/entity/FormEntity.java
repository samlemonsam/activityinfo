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
package org.activityinfo.store.hrd.entity;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.columns.RecordNumbering;

/**
 * Parent entity of all entities related to a single form.
 * 
 * <p>The {@code Form} entity forms the root of the Form entity group, allowing us to offer
 * Serializable Consistency with respect to the records within a single Form. </p>
 * 
 */
@Entity(name = "Form")
public class FormEntity {

    @Id
    private String id;

    /**
     * The current version of this Form. The {@code version} is incremented whenever the {@link FormSchemaEntity} is
     * changed for this Form Entity Group, or if any changes are made to the {@link FormRecordEntity}s that belong
     * to this Form Group.
     */
    @Unindex
    private long version;

    /**
     * The current version of this Form's {@link FormSchemaEntity}. The {@code schemaVersion} will always
     * be less than or equal to the Form's overall {@code version}.
     */
    @Unindex
    private long schemaVersion;


    /**
     * Which of the two recordCount indexes is currently active, RED or BLUE. This allows records to be
     * renumbered without locking the whole form for modifications.
     */
    @Unindex
    private RecordNumbering activeColumnStorage;


    public FormEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(ResourceId id) {
        this.id = id.asString();
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public static Key<FormEntity> key(ResourceId formId) {
        return Key.create(FormEntity.class, formId.asString());
    }

    public static Key<FormEntity> key(FormClass formClass) {
        return key(formClass.getId());
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(long schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public RecordNumbering getActiveColumnStorage() {
        return activeColumnStorage;
    }

    public void setActiveColumnStorage(RecordNumbering activeColumnStorage) {
        this.activeColumnStorage = activeColumnStorage;
    }
}
