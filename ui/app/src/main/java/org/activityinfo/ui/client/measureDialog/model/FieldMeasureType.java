package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.analysis.model.CountMeasure;

public class FieldMeasureType implements MeasureType {

    private ResourceId rootFormId;
    private FormField field;

    public FieldMeasureType(ResourceId rootFormId, FormField field) {
        this.rootFormId = rootFormId;
        this.field = field;
    }

    @Override
    public String getId() {
        return field.getId().asString();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public CountMeasure buildModel(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldMeasureType that = (FieldMeasureType) o;

        if (!rootFormId.equals(that.rootFormId)) {
            return false;
        }
        return field.getId().equals(that.field.getId());

    }

    @Override
    public int hashCode() {
        int result = rootFormId.hashCode();
        result = 31 * result + field.getId().hashCode();
        return result;
    }
}
