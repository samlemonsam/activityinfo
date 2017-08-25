package org.activityinfo.model.query;

public interface EnumColumnView extends ColumnView {

    /*
     * Allows for return of Enum ID values as opposed to their Label values
     */
    String getId(int row);

}
