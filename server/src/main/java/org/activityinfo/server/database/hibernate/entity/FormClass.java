package org.activityinfo.server.database.hibernate.entity;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;

/**
 * @author yuriyz on 02/11/2015.
 */
@Entity
public class FormClass implements Serializable, HasFormClassJson {

    private String id;
    private String ownerId;
    private String formClass;
    private byte[] gzFormClass;

    public FormClass() {
    }

    public FormClass(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "formClassId", unique = true, nullable = false)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "formClassOwnerId", unique = false, nullable = true)
    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Lob
    @Column(name = "json")
    public String getFormClass() {
        return formClass;
    }

    public void setFormClass(String formClass) {
        this.formClass = formClass;
    }

    @Column(name = "gzJson")
    public byte[] getGzFormClass() {
        return gzFormClass;
    }

    public void setGzFormClass(byte[] gzFormClass) {
        this.gzFormClass = gzFormClass;
    }
}
