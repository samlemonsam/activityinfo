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

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.CursorObserver;
import org.activityinfo.store.spi.PendingSlot;

/**
 * Counts the number of rows in a set
 */
public class RowCountBuilder implements CursorObserver<ResourceId> {

    private final PendingSlot<Integer> resultSlot;

    private int count = 0;

    public RowCountBuilder(PendingSlot<Integer> resultSlot) {
        this.resultSlot = resultSlot;
    }

    @Override
    public void onNext(ResourceId value) {
        count++;
    }

    @Override
    public void done() {
        resultSlot.set(count);
    }

    public int getCount() {
        return count;
    }
}
