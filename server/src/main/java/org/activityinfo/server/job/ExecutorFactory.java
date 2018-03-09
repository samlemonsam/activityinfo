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

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.activityinfo.model.job.ExportAuditLog;
import org.activityinfo.model.job.ExportFormJob;

/**
 * Creates Jobs based on their type id
 */
public class ExecutorFactory {

    private Injector injector;

    @Inject
    public ExecutorFactory(Injector injector) {
        this.injector = injector;
    }

    public JobExecutor create(String type) {
        if(type.equals(ExportFormJob.TYPE)) {
            return injector.getInstance(ExportFormExecutor.class);
        } else if(type.equals(ExportAuditLog.TYPE)) {
            return injector.getInstance(ExportAuditLogExecutor.class);
        }
        throw new IllegalArgumentException("No such type " + type);
    }
}
