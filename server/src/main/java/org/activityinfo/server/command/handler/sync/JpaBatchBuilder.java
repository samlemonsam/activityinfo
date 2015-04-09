package org.activityinfo.server.command.handler.sync;

import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.collect.Lists;
import org.activityinfo.server.database.hibernate.entity.Offline;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;


public class JpaBatchBuilder {

    private SqliteBatchBuilder batch;
    private EntityManager entityManager;


    public JpaBatchBuilder(SqliteBatchBuilder batch, EntityManager entityManager) {
        this.batch = batch;
        this.entityManager = entityManager;
    }

    public JpaBatchBuilder(EntityManager entityManager) throws IOException {
        this.batch = new SqliteBatchBuilder();
        this.entityManager = entityManager;
    }

    public void createTableIfNotExists(Class<?> entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS " + tableName(entity) + "(");

        boolean needsComma = false;

        for (SingularAttribute<?,?> attribute : attributesToSync(entity)) {
            if(needsComma) {
                sql.append(", ");
            }
            sql.append(sqliteType(attribute));
            sql.append(" ");
            sql.append(columnName(attribute));
            needsComma = true;
        }
        sql.append(")");
    }

    /**
     * Appends a delete statement to the update list, deleting all entities in the 
     * local browser database that match criteria.
     * @param entity a JPA entity
     * @param criteria Sqlite criteria
     */
    public void delete(Class<?> entity, String criteria) throws IOException {
        batch.addStatement("DELETE FROM " + tableName(entity) + " WHERE " + criteria);
    }


    private String tableName(Class<?> entity) {
        Table tableAnnotation = entity.getAnnotation(Table.class);
        String tableName;
        if(tableAnnotation != null) {
            tableName = tableAnnotation.name();
        } else {
            tableName = entity.getSimpleName();
        }

        return tableName.toLowerCase();
    }

    public void insert(Class<?> entity, String criteria) {
        SqlQuery query = SqlQuery.select(columnsToSync(entity)).from(tableName(entity)).whereTrue(criteria);
        batch.insert().into(tableName(entity))
                .from(query)
                .execute(entityManager);
    }
    
    public SqlQuery select(Class<?> entity) {
        return SqlQuery.select(columnsToSync(entity)).from(tableName(entity));
    }

    private List<SingularAttribute<?, ?>> attributesToSync(Class<?> entity) {
        List<SingularAttribute<?, ?>> attributes = new ArrayList<>();
        EntityType<?> entityType = entityManager.getMetamodel().entity(entity);
        for (SingularAttribute<?,?> attribute : entityType.getSingularAttributes()) {
            if(isSynced(attribute)) {
                if(attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                    addEmbeddedAttributes(attributes, attribute);
                } else {
                    attributes.add(attribute);
                }
            }
        }
        return attributes;
    }


    public void addStatement(String sql) throws IOException {
        batch.addStatement(sql);
    }

    private void addEmbeddedAttributes(List<SingularAttribute<?, ?>> columns, SingularAttribute<?, ?> attribute) {
        EmbeddableType<?> embeddedType = entityManager.getMetamodel().embeddable(attribute.getJavaType());
        for (SingularAttribute<?,?> embeddedAttribute : embeddedType.getSingularAttributes()) {
            if(isSynced(embeddedAttribute)) {
                columns.add(embeddedAttribute);
            }
        }
    }

    private String[] columnsToSync(Class<?> entity) {
        List<String> columns = Lists.newArrayList();
        for (SingularAttribute<?,?> attribute : attributesToSync(entity)) {
            if(isSynced(attribute)) {
                columns.add(columnName(attribute));
            }
        }
        return columns.toArray(new String[columns.size()]);
    }

    private String columnName(SingularAttribute<?, ?> attribute) {
        AccessibleObject member = (AccessibleObject) attribute.getJavaMember();
        String columnName = attribute.getName();
        Column column = member.getAnnotation(Column.class);
        if(column != null) {
            if(!column.name().isEmpty()) {
                columnName = column.name();
            }
        }
        JoinColumn joinColumn = member.getAnnotation(JoinColumn.class);
        if(joinColumn != null && joinColumn.name() != null) {
            columnName = joinColumn.name();
        }
        return columnName;
    }

    private boolean isSynced(SingularAttribute<?, ?> attribute) {
        AccessibleObject member = (AccessibleObject) attribute.getJavaMember();
        Offline offline = member.getAnnotation(Offline.class);
        if(offline == null) {
            return true;
        } else {
            return offline.sync();
        }
    }

    private String sqliteType(SingularAttribute<?,?> attribute) {

        if(attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE) {

            return findIdType(attribute.getJavaType());

        } else {

            return SqliteTypes.getSqliteType(attribute.getJavaType());

        }
    }

    private String findIdType(Class<?> javaType) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(javaType);
        for (SingularAttribute<?,?> attribute : entityType.getSingularAttributes()) {
            if(attribute.isId()) {
                return sqliteType(attribute);
            }
        }
        throw new IllegalArgumentException(javaType + " has no @Id field!");

    }

    public String build() throws IOException {
        return batch.build();
    }

    public SqliteInsertBuilder insert() {
        return batch.insert();
    }

}
