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

import com.bedatadriven.rebar.async.NullCallback;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class CommandRequestTest {

    @Test
    public void equalCommandsShouldBeMerged() {

        assumeThat(new GetSchema(), is(equalTo(new GetSchema())));

        CommandRequest firstCommand = new CommandRequest(new GetSchema(),
                new NullCallback());
        List<CommandRequest> pending = Collections.singletonList(firstCommand);

        CommandRequest secondRequest = new CommandRequest(new GetSchema(),
                new NullCallback());

        boolean merged = secondRequest.mergeSuccessfulInto(pending);

        assertThat("merged", merged, is(true));
        assertThat(firstCommand.getCallbacks(), hasItem(first(secondRequest.getCallbacks())));

    }

    private <T> T first(Collection<T> items) {
        return items.iterator().next();
    }

}
