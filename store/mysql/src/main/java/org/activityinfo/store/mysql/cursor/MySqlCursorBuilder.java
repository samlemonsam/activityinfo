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
package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.eval.FieldReader;
import org.activityinfo.model.formula.eval.FieldReaderFactory;
import org.activityinfo.model.formula.eval.PartialEvaluator;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.mapping.FieldMapping;
import org.activityinfo.store.mysql.mapping.ResourceIdConverter;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.spi.Cursor;
import org.activityinfo.store.spi.CursorBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.util.Iterator;


public class MySqlCursorBuilder implements CursorBuilder {

    private TableMapping tableMapping;
    private SqlBuilder query;
    private final PartialEvaluator<ResultSet> evaluator;
    private QueryExecutor executor;
    private MySqlCursor cursor;
    private boolean open = false;

    public MySqlCursorBuilder(TableMapping tableMapping, QueryExecutor executor) {
        this.tableMapping = tableMapping;
        this.executor = executor;
        this.query = new SqlBuilder(tableMapping.getBaseFromClause());
        this.query.where(tableMapping.getBaseFilter());
        this.query.orderBy(tableMapping.getPrimaryKey().getColumnName());
        this.evaluator = new PartialEvaluator<>(tableMapping.getFormClass(), readerFactory);
        this.cursor = new MySqlCursor();
        this.cursor.primaryKey = primaryKeyExtractor();
    }

    private ResourceIdConverter primaryKeyExtractor() {
        String column = tableMapping.getPrimaryKey().getColumnName();
        int index = query.select(column);

        return new ResourceIdConverter(tableMapping.getPrimaryKey().getDomain(), index);
    }
    
    public void only(ResourceId resourceId) {
        query.where("base." + tableMapping.getPrimaryKey().getColumnName() + "=" + CuidAdapter.getLegacyIdFromCuid(resourceId));
    }

    public void where(String where) {
        query.where(where);
    }

    @Override
    public void addResourceId(final CursorObserver<ResourceId> observer) {
        cursor.onNext.add(new Runnable() {
            @Override
            public void run() {
                observer.onNext(cursor.getResourceId());
            }
        });
        cursor.onClosed.add(observer);
    }

    @Override
    public void addField(FormulaNode node, CursorObserver<FieldValue> observer) {
        final FieldReader<ResultSet> reader = evaluator.partiallyEvaluate(node);
        addFieldObserver(reader, observer);
    }

    @Override
    public void addField(FieldPath fieldPath, CursorObserver<FieldValue> observer) {
        if(fieldPath.getDepth() > 1) {
            throw new UnsupportedOperationException("path: " + fieldPath);
        }
        addField(fieldPath.getRoot(), observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        FieldReader<ResultSet> reader = createFieldReader(fieldId);
        addFieldObserver(reader, observer);
    }

    private void addFieldObserver(final FieldReader<ResultSet> reader, final CursorObserver<FieldValue> observer) {
        Preconditions.checkState(!open, "Fields cannot be added after the cursor is open.");
        cursor.onNext.add(new Runnable() {
            @Override
            public void run() {
                FieldValue value = reader.readField(cursor.resultSet);
                observer.onNext(value);
            }
        });
        cursor.onClosed.add(observer);
    }

    private FieldReaderFactory<ResultSet> readerFactory = new FieldReaderFactory<ResultSet>() {
        @Override
        public FieldReader<ResultSet> create(FormField field) {
            return createFieldReader(field.getId());
        }
    };

    private FieldReader<ResultSet> createFieldReader(ResourceId fieldId) {
        FieldMapping mapping = tableMapping.getMapping(fieldId);
        if(mapping == null) {
            throw new IllegalArgumentException(String.format("No such field %s in %s",
                    fieldId,
                    tableMapping.getFormClass().getId()));
        }

        // Add all columns required for this field
        Iterator<String> it = mapping.getColumnNames().iterator();

        int startIndex = query.select(mapping.getJoin(), it.next());
        while(it.hasNext()) {
            query.select(mapping.getJoin(), it.next());
        }

        return new ResultSetFieldReader(startIndex, mapping.getConverter(), mapping.getFormField().getType());
    }
    
    public SqlBuilder getQuery() {
        return query;
    }
    
    @Override
    public Cursor open() {
        String sql = query.buildSQL();

        System.out.println(sql);

        try {
            cursor.resultSet = executor.query(sql);
        } catch(Exception e) {
            throw new RuntimeException("Failed to execute query cursor on " + tableMapping + ". SQL: " + sql, e);
        }

        open = true;
        return cursor;
    }

    public boolean hasObservers() {
        return cursor.hasObservers();
    }
}
