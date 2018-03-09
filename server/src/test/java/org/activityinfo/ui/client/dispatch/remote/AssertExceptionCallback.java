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

import com.google.api.client.util.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.List;

/**
 * @author yuriyz on 10/27/2014.
 */
public class AssertExceptionCallback implements AsyncCallback {

    private final List<Throwable> exceptions = Lists.newArrayList();

    @Override
    public void onFailure(Throwable throwable) {
        exceptions.add(throwable);
    }

    @Override
    public void onSuccess(Object o) {
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    public Throwable getLastException() {
        return exceptions.get(0);
    }

    public void assertInstanceof(Class clazz) {
        Assert.assertThat(getLastException(), Matchers.instanceOf(clazz));
    }

}
