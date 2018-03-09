/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
