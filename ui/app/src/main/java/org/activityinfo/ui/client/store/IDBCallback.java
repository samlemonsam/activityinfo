package org.activityinfo.ui.client.store;


import com.google.gwt.core.client.JavaScriptObject;

public interface IDBCallback<T> {

    void onSuccess(T result);

    void onFailure(JavaScriptObject error);


}
