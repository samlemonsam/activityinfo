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
package org.activityinfo.server.endpoint.export;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.lightoze.gwt.i18n.server.ThreadLocalLocaleProvider;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.generated.GeneratedResource;
import org.activityinfo.server.generated.StorageProvider;
import org.activityinfo.server.util.monitoring.Timed;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

@Singleton
public class ExportSitesTask extends HttpServlet {

    public static final String END_POINT = "/tasks/export";

    private final Provider<DispatcherSync> dispatcher;
    private final ServerSideAuthProvider authProvider;
    private final StorageProvider storageProvider;
    
    @Inject
    public ExportSitesTask(Provider<DispatcherSync> dispatcher, 
                           ServerSideAuthProvider authProvider,
                           StorageProvider storageProvider) {
        this.dispatcher = dispatcher;
        this.authProvider = authProvider;
        this.storageProvider = storageProvider;
    }

    @Override
    @Timed(name = "export", kind = "sites")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String exportId = req.getParameter("exportId");
        String locale = req.getParameter("locale");
        if(Strings.isNullOrEmpty(locale)) {
            locale = Locale.ENGLISH.toLanguageTag();
        }
        
        // authenticate this task
        authProvider.set(new AuthenticatedUser("",
                Integer.parseInt(req.getParameter("userId")),
                req.getParameter("userEmail")));

        ThreadLocalLocaleProvider.pushLocale(Locale.forLanguageTag(locale));
        
        try {
            // create the workbook
            Filter filter = FilterUrlSerializer.fromQueryParameter(req.getParameter("filter"));
            TaskContext context = new TaskContext(dispatcher.get(), storageProvider, exportId);
            SiteExporter export = new SiteExporter(context).buildExcelWorkbook(filter);

            // Save to Export storage
            GeneratedResource storage = storageProvider.get(exportId);
            try (OutputStream out = storage.openOutputStream()) {
                export.getBook().write(out);
            }
        } finally {
            ThreadLocalLocaleProvider.popLocale();
        }
    }
}
