package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;

public class ActivityIdFieldBinding extends ClassIdFieldBinding {

    private static final String ACTIVITY_ID_FIELD = "activityId";

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView activityId = columnSet.getColumnView(CLASS_ID_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            dataArray[i].set(ACTIVITY_ID_FIELD, CuidAdapter.getLegacyIdFromCuid(activityId.getString(i)));
        }

        return dataArray;
    }

}
