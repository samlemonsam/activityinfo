package org.activityinfo.geoadmin.merge2.view.swing.table;


public interface ColumnFunction<T> {

    public String getHeader();
    
    public String getCell(T source, int row);
    


}
