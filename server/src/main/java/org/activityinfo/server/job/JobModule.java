package org.activityinfo.server.job;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.Provides;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.server.endpoint.rest.RestApiModule;
import org.activityinfo.store.query.impl.AppEngineFormScanCache;
import org.activityinfo.store.query.server.FormSourceSyncImpl;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.spi.FormCatalog;

public class JobModule extends RestApiModule {
    @Override
    protected void configureResources() {
        bindResource(JobResource.class);
        serve(JobTaskServlet.END_POINT).with(JobTaskServlet.class);
    }

    @Provides
    public FormSource provideFormSource(FormCatalog catalog, AuthenticatedUser user) {
        return new FormSourceSyncImpl(catalog, new AppEngineFormScanCache(), user.getUserId());
    }
}