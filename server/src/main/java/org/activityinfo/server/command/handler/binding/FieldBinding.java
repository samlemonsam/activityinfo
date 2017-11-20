package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public interface FieldBinding<M extends BaseModelData> {

    public M[] extractFieldData(M[] dataArray, ColumnSet columnSet);

    public List<ColumnModel> getColumnQuery(FormTree formTree);

    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId);

}
