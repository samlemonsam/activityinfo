package org.activityinfo.observable;


public interface TableObserver {
    
    void onRowsChanged();
    
    void onRowChanged(int index);
    
    
    
}
