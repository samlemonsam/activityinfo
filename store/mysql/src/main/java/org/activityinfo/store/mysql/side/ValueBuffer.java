package org.activityinfo.store.mysql.side;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
* Created by alex on 1/20/15.
*/
interface ValueBuffer {
    
    public static final int ROW_ID_COLUMN  = 1;
    public static final int FIELD_ID_COLUMN = 2;
    public static final int DOUBLE_VALUE_COLUMN = 3;
    public static final int STRING_VALUE_COLUMN = 4;

    public static final int ATTRIBUTE_ID_COLUMN = 3;
    public static final int ATTRIBUTE_VALUE_COLUMN = 4;

    void set(ResultSet rs) throws SQLException;
    void next();
    void done();
}
