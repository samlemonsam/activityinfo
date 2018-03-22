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
package org.activityinfo.server.database.hibernate.entity;

import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceBuilder;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.legacy.CuidAdapter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@EntityListeners(SchemaChangeListener.class)
public class Folder implements SchemaElement, Serializable, HardDeleteable {

    private int id;
    private String name;
    private Database database;
    private int sortOrder;

    public Folder() {
    }

    public Folder(Folder folder) {
        this.name = folder.name;
        this.sortOrder = folder.sortOrder;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FolderId", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatabaseId") @NotNull
    public Database getDatabase() {
        return database;
    }

    public void setId(int id) {
        this.id = id;
    }


    @NotNull
    @Size(max = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Offline
    @Column(nullable = false)
    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public Database findOwningDatabase() {
        return database;
    }


    public Resource asResource() {
        return new ResourceBuilder()
            .setId(CuidAdapter.folderId(id))
            .setParentId(CuidAdapter.databaseId(database.getId()))
            .setLabel(getName())
            .setType(ResourceType.FOLDER)
            .build();
    }

    @Override
    public void delete() {
        // NOOP, folder is removed directly from database
    }
}
