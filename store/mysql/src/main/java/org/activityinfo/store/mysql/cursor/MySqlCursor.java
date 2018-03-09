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
package org.activityinfo.store.mysql.cursor;

import com.google.common.collect.Lists;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.mapping.ResourceIdConverter;
import org.activityinfo.store.spi.Cursor;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


class MySqlCursor implements Cursor {

    ResultSet resultSet;
    ResourceIdConverter primaryKey;

    List<Runnable> onNext = Lists.newArrayList();
    List<CursorObserver> onClosed = Lists.newArrayList();

    @Override
    public boolean next() {
        try {
            boolean hasNext = resultSet.next();
            if(hasNext) {
                for(Runnable observer : onNext) {
                    observer.run();
                }
            } else {
                for(CursorObserver observer : onClosed) {
                    observer.done();
                }
            }
            return hasNext;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResourceId getResourceId() {
        return primaryKey.toResourceId(resultSet);
    }

    public boolean hasObservers() {
        return onNext.size() > 0 || onClosed.size() > 0;
    }
}
