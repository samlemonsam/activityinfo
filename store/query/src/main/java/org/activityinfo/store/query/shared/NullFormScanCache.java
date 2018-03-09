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
package org.activityinfo.store.query.shared;

import com.google.common.util.concurrent.Futures;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


public class NullFormScanCache implements FormScanCache {
    @Override
    public Map<String, Object> getAll(Set<String> keys) {
        return Collections.emptyMap();
    }

    @Override
    public Future<Integer> enqueuePut(Map<String, Object> toPut) {
        return Futures.immediateFuture(0);
    }

    @Override
    public void waitForCachingToFinish(List<Future<Integer>> pendingCachePuts) {
        // NOOP
    }
}