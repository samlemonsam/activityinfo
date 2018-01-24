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
    private UserDatabase userDatabase;

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
    public UserDatabase getUserDatabase() {
        return userDatabase;
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

    public void setUserDatabase(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    @Override
    public UserDatabase findOwningDatabase() {
        return userDatabase;
    }


}
