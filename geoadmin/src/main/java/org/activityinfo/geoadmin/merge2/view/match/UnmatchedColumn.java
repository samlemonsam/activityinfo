package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.query.ColumnView;


/**
 * Displays the contents of a Field in the source or target collection that is not matched. 
 */
public class UnmatchedColumn extends MatchTableColumn {

    private final FieldProfile field;
    private ColumnView columnView;

    public UnmatchedColumn(FieldProfile field, ColumnView columnView) {
        this.field = field;
        this.columnView = columnView;
    }

    @Override
    public String getHeader() {
        return field.getLabel();
    }

    @Override
    public String getValue(int rowIndex) {
        if(field.getView() != null) {
            Object value = columnView.get(rowIndex);
            if(value != null) {
                return value.toString();
            }
        }
        return null;
    }

}
