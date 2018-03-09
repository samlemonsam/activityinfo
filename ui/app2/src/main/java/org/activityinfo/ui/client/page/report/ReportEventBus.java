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
package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.event.Listener;
import org.activityinfo.ui.client.EventBus;

public class ReportEventBus {

    private EventBus eventBus;
    private HasReportElement source;
    private Listener<ReportChangeEvent> innerListener;

    public ReportEventBus(final EventBus eventBus, final HasReportElement source) {
        this.eventBus = eventBus;
        this.source = source;
    }

    public void fireChange() {
        eventBus.fireEvent(new ReportChangeEvent(source, source.getModel()));
    }

    public void listen(final ReportChangeHandler handler) {
        innerListener = new Listener<ReportChangeEvent>() {

            @Override
            public void handleEvent(ReportChangeEvent be) {
                if (be.getModel() == source.getModel() && be.getSource() != source) {
                    handler.onChanged();
                }
            }
        };
        eventBus.addListener(ReportChangeEvent.TYPE, innerListener);
    }

    public void disconnect() {
        if (innerListener != null) {
            eventBus.removeListener(ReportChangeEvent.TYPE, innerListener);
        }
    }
}
