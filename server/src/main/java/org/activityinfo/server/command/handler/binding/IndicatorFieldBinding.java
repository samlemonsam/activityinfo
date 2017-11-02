package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class IndicatorFieldBinding implements FieldBinding {

    private ResourceId indicatorId;
    private int legacyId;

    public IndicatorFieldBinding(ResourceId indicatorId) {
        this.indicatorId = indicatorId;
        this.legacyId = CuidAdapter.getLegacyIdFromCuid(indicatorId);
    }

    public IndicatorFieldBinding(int indicatorId) {
        this.indicatorId = CuidAdapter.indicatorField(indicatorId);
        this.legacyId = indicatorId;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView indicator = columnSet.getColumnView(indicatorId.toString());

        for (int i=0; i<columnSet.getNumRows(); i++) {
            Object value = indicator.get(i);
            if (!value.equals(Double.NaN)) {
                dataArray[i].set(IndicatorDTO.getPropertyName(legacyId), indicator.get(i));
            }
        }

        return dataArray;
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
