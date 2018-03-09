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
package org.activityinfo.ui.client.dispatch.remote;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuriyz on 10/27/2014.
 */
public class TimeoutDispatcherMock extends AbstractDispatcher {

    private AtomicInteger executeCounter = new AtomicInteger(0);

    @Override
    public <T extends CommandResult> void execute(Command<T> command, AsyncCallback<T> callback) {
        executeCounter.incrementAndGet();
        callback.onFailure(new UnexpectedCommandException());
    }

    public int getExecuteCounter() {
        return executeCounter.get();
    }
}

