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
package org.activityinfo.store.query.shared.columns;

import com.google.common.collect.Lists;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.shared.PendingSlot;
import org.activityinfo.store.spi.CursorObserver;

import java.util.List;

public class IdColumnBuilder implements CursorObserver<ResourceId> {

    private final PendingSlot<ColumnView> result;
    private final List<String> ids = Lists.newArrayList();

    public IdColumnBuilder(PendingSlot<ColumnView> result) {
        this.result = result;
    }

    @Override
    public void onNext(ResourceId resourceId) {
        ids.add(resourceId.asString());
    }

    @Override
    public void done() {
        result.set(new StringArrayColumnView(ids));
    }

    public ColumnView get() {
        return result.get();
    }
}
