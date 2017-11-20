package org.activityinfo.ui.client.store;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.ui.client.store.tasks.RefetchHandler;
import org.activityinfo.ui.client.store.tasks.Watcher;

/**
 * Fired when an analysis changes.
 */
public class AnalysisChangeEvent extends GwtEvent<AnalysisChangeEventHandler> {

    public static Type<AnalysisChangeEventHandler> TYPE = new Type<AnalysisChangeEventHandler>();

    private String analysisId;

    public AnalysisChangeEvent(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public Type<AnalysisChangeEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(AnalysisChangeEventHandler handler) {
        handler.onAnalysisChanged(this);
    }

    public static Watcher watchFor(EventBus eventBus, String analysisId) {
        return new Watcher() {

            private HandlerRegistration registration;

            @Override
            public void start(RefetchHandler handler) {
                registration = eventBus.addHandler(AnalysisChangeEvent.TYPE, new AnalysisChangeEventHandler() {
                    @Override
                    public void onAnalysisChanged(AnalysisChangeEvent event) {
                        if (event.getAnalysisId().equals(analysisId)) {
                            handler.refetch();
                        }
                    }
                });
            }

            @Override
            public void stop() {
                registration.removeHandler();
                registration = null;
            }
        };
    }
}
