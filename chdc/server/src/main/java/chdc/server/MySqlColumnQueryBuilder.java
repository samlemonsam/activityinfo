package chdc.server;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.CursorObserver;

import java.util.ArrayList;
import java.util.List;

public class MySqlColumnQueryBuilder implements ColumnQueryBuilder {

    private class FieldExtractor {
        private int columnIndex;
        private final String fieldId;
        private final CursorObserver<FieldValue> observer;

        private FieldExtractor(int columnIndex, String fieldId, CursorObserver<FieldValue> observer) {
            this.columnIndex = columnIndex;
            this.fieldId = fieldId;
            this.observer = observer;
        }
    }

    private FormClass schema;
    private StringBuilder fieldList = new StringBuilder();

    private List<CursorObserver<ResourceId>> idObservers = new ArrayList<>(0);
    private List<FieldExtractor> fieldExtractors = new ArrayList<>();

    public MySqlColumnQueryBuilder(FormClass schema) {
        this.schema = schema;
    }

    @Override
    public void only(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        idObservers.add(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        fieldExtractors.add(new FieldExtractor(columnIndex, fieldId.asString(), observer));
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("TODO");
    }
}
