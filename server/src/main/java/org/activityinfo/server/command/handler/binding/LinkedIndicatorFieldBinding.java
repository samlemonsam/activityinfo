package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnView;

public class LinkedIndicatorFieldBinding extends IndicatorFieldBinding {

    private final int destinationId;

    public LinkedIndicatorFieldBinding(int destinationId, FormField indicatorField) {
        super(indicatorField);
        this.destinationId = destinationId;
    }

    public LinkedIndicatorFieldBinding(FormField indicatorField) {
        super(indicatorField);
        this.destinationId = CuidAdapter.getLegacyIdFromCuid(indicatorField.getId());
    }

    @Override
    protected void getQuantityIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            Double value = indicatorColumn.getDouble(i);
            if (value != null && !value.isNaN()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(destinationId), value);
            }
        }
    }

    @Override
    protected void getTextIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            String value = indicatorColumn.getString(i);
            if (value != null && !value.isEmpty()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(destinationId), value);
            }
        }
    }
}
