package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.query.ColumnView;


/**
 * Displays the contents of a Field in the source or target collection that is not matched. 
 */
public class UnmatchedColumn extends MatchTableColumn {

    private final FieldProfile field;
    private final MatchSide side;
    private ColumnView columnView;

    public UnmatchedColumn(FieldProfile field, MatchSide side, ColumnView columnView) {
        this.field = field;
        this.side = side;
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

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.of(side);
    }

}
