package org.activityinfo.store.query.impl.views;

import org.activityinfo.model.query.ColumnView;

public class ColumnViewToString {
    
    public static String toString(ColumnView columnView) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i!=Math.min(10, columnView.numRows());++i) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(columnView.get(i));
        }
        if(columnView.numRows() >= 10) {
            sb.append("... numRows = ").append(columnView.numRows());
        }
        sb.append("]");
        return sb.toString();
    }
}
