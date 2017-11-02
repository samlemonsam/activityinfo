package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

// TODO
public class GeoAreaFieldBinding implements FieldBinding {
    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        return new BaseModelData[0];
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return null;
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
