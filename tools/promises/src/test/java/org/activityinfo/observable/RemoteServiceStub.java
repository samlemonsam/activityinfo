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
package org.activityinfo.observable;

import java.util.ArrayList;
import java.util.List;

public class RemoteServiceStub {

    private List<RemoteCall> pendingCalls = new ArrayList<>();
    
    public Observable<String> queryName(int id) {
        RemoteCall call = new RemoteCall(id);
        pendingCalls.add(call);
        return call.value;
    }

    public void completePending() {
        for (RemoteCall pendingCall : pendingCalls) {
            pendingCall.complete();
        }
        pendingCalls.clear();
    }


    private static class RemoteCall {
        private int id;
        private ObservableStub<String> value = new ObservableStub<>();

        public RemoteCall(int id) {
            this.id = id;
        }

        public void complete() {
            value.updateValue("name" + id);
        }
    }
    
    
    
}
