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
package org.activityinfo.ui.client.local.capability;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides information about the current browser's capability for offline mode.
 * <p/>
 * This default implementation is for fully supported browsers: webkit and
 * opera.
 */
public class LocalCapabilityProfile {

    public boolean isOfflineModeSupported() {
        return false;
    }

    /**
     * Acquire all necessary permissions from the user to use offline mode.
     *
     * @param callback
     * @throws UnsupportedOperationException if this browser does not support offline mode
     */
    public void acquirePermission(AsyncCallback<Void> callback) {
        callback.onFailure(new UnsupportedOperationException());
    }
}
