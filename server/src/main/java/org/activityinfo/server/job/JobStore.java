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
package org.activityinfo.server.job;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

/**
 * ObjectifyService wrapper which ensures entity classes are registered.
 */
public class JobStore {
    static {
        ObjectifyService.register(JobEntity.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static LoadResult<JobEntity> getUserJob(String websafeKey) {
        return ofy().load().key(Key.<JobEntity>create(websafeKey));
    }

    public static String getWebSafeKeyString(JobEntity job) {
        return Key.create(job).toWebSafeString();
    }
}
