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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


public interface FormScanCache {

    /**
     * Retrieves all requested keys synchronously.
     */
    Map<String, Object> getAll(Set<String> keys);

    /**
     * Starts an asynchronous request to cache the given key/value pairs.
     * @return a Future containing the number of columns cached.
     */
    Future<Integer> enqueuePut(Map<String, Object> toPut);


    /**
     * Wait for caching to complete, if there is still enough time left in this request.
     */
    void waitForCachingToFinish(List<Future<Integer>> pendingCachePuts);

}