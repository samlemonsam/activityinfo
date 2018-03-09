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
package org.activityinfo.ui.client.store.http;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.ui.client.store.tasks.Task;
import org.activityinfo.ui.client.store.tasks.TaskExecution;

class RequestTask<T> implements Task<T> {

    private HttpBus bus;
    private HttpRequest<T> request;

    public RequestTask(HttpBus bus, HttpRequest<T> request) {
        this.bus = bus;
        this.request = request;
    }

    @Override
    public TaskExecution start(AsyncCallback<T> callback) {
        return bus.submit(request, callback);
    }

    @Override
    public int refreshInterval(T result) {
        return request.refreshInterval(result);
    }
}
