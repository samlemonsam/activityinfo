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
package org.activityinfo.ui.client.component.importDialog;

import com.google.web.bindery.event.shared.Event;

public class PageChangedEvent extends Event<PageChangedEventHandler> {

    public static Type<PageChangedEventHandler> TYPE = new Type<PageChangedEventHandler>();

    private boolean valid;
    private String statusMessage;

    public PageChangedEvent(boolean valid, String statusMessage) {
        super();
        this.valid = valid;
        this.statusMessage = statusMessage;
    }

    @Override
    public Type<PageChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PageChangedEventHandler handler) {
        handler.onPageChanged(this);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
