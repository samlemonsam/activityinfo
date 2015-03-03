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

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author yuriyz on 02/11/2015.
 */
@Entity
@Table(name = "formclass")
public class FormClassEntity implements Serializable, HasJson {

    private String id;
    private String ownerId;
    private String formClassJson;
    private byte[] gzFormClassJson;

    public FormClassEntity() {
    }

    public FormClassEntity(String id) {
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
    public String getJson() {
        return formClassJson;
    }

    public void setJson(String formClass) {
        this.formClassJson = formClass;
    }

    @Column(name = "gzJson")
    public byte[] getGzJson() {
        return gzFormClassJson;
    }

    public void setGzJson(byte[] gzFormClass) {
        this.gzFormClassJson = gzFormClass;
    }
}
