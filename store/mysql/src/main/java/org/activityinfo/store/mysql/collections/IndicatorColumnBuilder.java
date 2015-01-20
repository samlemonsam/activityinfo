package org.activityinfo.store.mysql.collections;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class IndicatorColumnBuilder {

    private static final int ROW_ID_COLUMN = 1;
    private static final int INDICATOR_ID_COLUMN = 2;
    private static final int DOUBLE_VALUE_COLUMN = 3;
    private static final int TEXT_VALUE_COLUMN = 4;

    /**
     * Maps indicatorId to the value buffer
     */
    private Map<Integer, ValueBuffer> indicatorMap = Maps.newHashMap();
    
    private String baseTable;
    private QueryExecutor executor;
    private final String newLine;
    
    public IndicatorColumnBuilder(Activity activity, String baseTable, QueryExecutor executor) {
        this.baseTable = baseTable;
        this.executor = executor;
        this.newLine = "\n";
    }
    
    public void add(ActivityField field, final CursorObserver<FieldValue> observer) {
        final int columnIndex = indicatorMap.size();
        indicatorMap.put(field.getId(), createBuffer(field, observer));
    }

    private ValueBuffer createBuffer(ActivityField field, CursorObserver<FieldValue> observer) {

        if(field.getFormField().getType() instanceof QuantityType) {
            QuantityType type = (QuantityType) field.getFormField().getType();
            return new QuantityBuffer(type, observer);

        } else if(field.getFormField().getType() instanceof TextType) {
            return new TextBuffer(observer);

        } else {
            throw new IllegalArgumentException("type: " + field.getFormField().getType());
        }
    }


    public void execute() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT site.siteId, iv.indicatorId, iv.value, iv.textValue").append(newLine);
        sql.append("FROM site").append(newLine);
        sql.append("LEFT JOIN reportingperiod rp ON (site.siteId = rp.siteId)").append(newLine);
        sql.append("LEFT JOIN indicatorvalue iv ON (rp.reportingPeriodId = iv.reportingPeriodId)").append(newLine);
        sql.append("WHERE iv.indicatorId IN (");
        Joiner.on(", ").appendTo(sql, indicatorMap.keySet());
        sql.append(")").append(newLine);
        sql.append("ORDER BY site.siteId");

        int lastRowId = -1;
        
        ValueBuffer buffers[] = bufferArray();
        
        try(ResultSet rs = executor.query(sql.toString())) {
            while(rs.next()) {
                int rowId = rs.getInt(ROW_ID_COLUMN);
                if(rowId != lastRowId && lastRowId != -1) {
                    for(int i=0;i!=buffers.length;++i) {
                        buffers[i].next();
                    }
                }
                int indicatorId = rs.getInt(INDICATOR_ID_COLUMN);
                if(!rs.wasNull()) {
                    indicatorMap.get(indicatorId).set(rs);
                }
                lastRowId = rowId;
            }   
            if(lastRowId != -1) {
                for(int i=0;i!=buffers.length;++i) {
                    buffers[i].next();
                }
            }
            for(int i=0;i!=buffers.length;++i) {
                buffers[i].done();
            }
        }
    }

    private ValueBuffer[] bufferArray() {
        List<ValueBuffer> buffers = Lists.newArrayList(indicatorMap.values());
        return buffers.toArray(new ValueBuffer[buffers.size()]);
    }

    public boolean isEmpty() {
        return indicatorMap.isEmpty();
    }

    private interface ValueBuffer {
        void reset();
        void set(ResultSet rs) throws SQLException;
        void next();
        void done();
    }
    
    private class QuantityBuffer implements ValueBuffer {
        private String units;
        private Quantity value;
        private CursorObserver<FieldValue> observer;
        
        public QuantityBuffer(QuantityType type, CursorObserver<FieldValue> observer) {
            this.units = type.getUnits();
            this.observer = observer;
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public void set(ResultSet rs) throws SQLException {
            double doubleValue = rs.getDouble(DOUBLE_VALUE_COLUMN);
            if(rs.wasNull()) {
                value = null;
            } else {
                value = new Quantity(doubleValue, units);
            }
        }

        @Override
        public void next() {
            observer.onNext(value);
            value = null;
        }

        @Override
        public void done() {
            observer.done();
        }
    }
    
    private class TextBuffer implements ValueBuffer {
        private TextValue value = null;
        private CursorObserver<FieldValue> observer;

        public TextBuffer(CursorObserver<FieldValue> observer) {
            this.observer = observer;
        }

        @Override
        public void reset() {
            value = null;
        }

        @Override
        public void set(ResultSet rs) throws SQLException {
            value = TextValue.valueOf(rs.getString(TEXT_VALUE_COLUMN));
        }

        @Override
        public void next() {
            observer.onNext(value);
        }

        @Override
        public void done() {
            observer.done();
        }
    }
}
