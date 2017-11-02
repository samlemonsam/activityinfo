package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public abstract class ClassIdFieldBinding implements FieldBinding {

    public static final String CLASS_ID_COLUMN = ColumnModel.CLASS_SYMBOL;

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getTargetColumnQuery(formTree.getRootFormId());
    }

    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return Arrays.asList(
                new ColumnModel().setExpression(CLASS_ID_COLUMN).as(CLASS_ID_COLUMN)
        );
    }

}
