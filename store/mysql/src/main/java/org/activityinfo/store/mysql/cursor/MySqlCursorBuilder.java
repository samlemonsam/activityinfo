package org.activityinfo.store.mysql.cursor;

import com.google.common.base.Preconditions;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.eval.FieldReader;
import org.activityinfo.model.expr.eval.FieldReaderFactory;
import org.activityinfo.model.expr.eval.PartialEvaluator;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.service.store.CursorBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.mapping.FieldMapping;
import org.activityinfo.store.mysql.mapping.PrimaryKeyExtractor;
import org.activityinfo.store.mysql.mapping.TableMapping;

import java.sql.ResultSet;


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
        this.query = new SqlBuilder(tableMapping.getBaseTable());
        this.evaluator = new PartialEvaluator<>(tableMapping.getFormClass(), readerFactory);
        this.cursor = new MySqlCursor();
        this.cursor.primaryKey = primaryKeyExtractor();
    }

    private PrimaryKeyExtractor primaryKeyExtractor() {
        String column = tableMapping.getPrimaryKey().getColumnName();
        int index = query.select(column);

        return new PrimaryKeyExtractor(tableMapping.getPrimaryKey().getDomain(), index);
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
    public void addField(ExprNode node, CursorObserver<FieldValue> observer) {
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
        int columnIndex = query.select(mapping.getColumnName());
        return new ResultSetFieldReader(columnIndex, mapping.getValueExtractor(), mapping.getFormField().getType());
    }

    @Override
    public Cursor open() {
        String sql = query.buildSQL();
        try {
            cursor.resultSet = executor.query(sql);
        } catch(Exception e) {
            throw new RuntimeException("Failed to execute query cursor on " + tableMapping + ". SQL: " + sql, e);
        }

        open = true;
        return cursor;
    }
}
