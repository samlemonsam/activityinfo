package org.activityinfo.ui.client.component.importDialog;

public interface ImportCallback {
    void onLoaded();
    void onFailure(Throwable reason);
    void onComplete();
}
