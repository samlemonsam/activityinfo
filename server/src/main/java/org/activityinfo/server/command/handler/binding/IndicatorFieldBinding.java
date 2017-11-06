package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;

import java.util.Arrays;
import java.util.List;

public class IndicatorFieldBinding implements FieldBinding {

    private FormField indicatorField;
    private ResourceId indicatorId;
    private int legacyId;

    public IndicatorFieldBinding(FormField indicatorField) {
        this.indicatorField = indicatorField;
        this.indicatorId = indicatorField.getId();
        this.legacyId = CuidAdapter.getLegacyIdFromCuid(indicatorId);
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView indicator = columnSet.getColumnView(indicatorId.toString());

        if (indicatorField.getType() instanceof QuantityType) {
            getQuantityIndicator(dataArray, indicator);
        } else if (indicatorField.getType() instanceof TextType || indicatorField.getType() instanceof NarrativeType) {
            getTextIndicator(dataArray, indicator);
        }

        return dataArray;
    }

    private void getQuantityIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            Double value = indicatorColumn.getDouble(i);
            if (value != null && !value.isNaN()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(legacyId), value);
            }
        }
    }

    private void getTextIndicator(BaseModelData[] dataArray, ColumnView indicatorColumn) {
        for (int i=0; i<indicatorColumn.numRows(); i++) {
            String value = indicatorColumn.getString(i);
            if (value != null && !value.isEmpty()) {
                dataArray[i].set(IndicatorDTO.getPropertyName(legacyId), value);
            }
        }
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return Arrays.asList(new ColumnModel().setExpression(indicatorId).as(indicatorId.toString()));
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Arrays.asList(new ColumnModel().setExpression(indicatorId).as(indicatorId.toString()));
    }
}
