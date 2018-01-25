package org.activityinfo.server.database.hibernate.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@EntityListeners(SchemaChangeListener.class)
public class Folder implements SchemaElement, Serializable  {

    private int id;
    private String name;
    private UserDatabase database;

    public Folder() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "FolderId", unique = true, nullable = false)
    public int getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatabaseId") @NotNull
    public UserDatabase getDatabase() {
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

    public void setDatabase(UserDatabase database) {
        this.database = database;
    }

    @Override
    public UserDatabase findOwningDatabase() {
        return database;
    }


}
