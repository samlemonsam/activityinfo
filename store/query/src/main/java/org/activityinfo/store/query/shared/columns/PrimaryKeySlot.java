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

import org.activityinfo.model.query.ColumnView;
import org.activityinfo.store.query.server.join.FastPrimaryKeyMap;
import org.activityinfo.store.query.shared.join.PrimaryKeyMap;
import org.activityinfo.store.spi.Slot;


public class PrimaryKeySlot implements Slot<PrimaryKeyMap> {

    private Slot<ColumnView> idSlot;
    private PrimaryKeyMap map;

    public PrimaryKeySlot(Slot<ColumnView> idSlot) {
        this.idSlot = idSlot;
    }

    @Override
    public PrimaryKeyMap get() {
        if(map == null) {
            map = new FastPrimaryKeyMap(idSlot.get());
        }
        return map;
    }
}
