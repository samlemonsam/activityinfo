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
package org.activityinfo.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.activityinfo.server.approval.ApprovalModule;
import org.activityinfo.server.attachment.AttachmentModule;
import org.activityinfo.server.authentication.AuthenticationModule;
import org.activityinfo.server.blob.GcsBlobFieldStorageServiceModule;
import org.activityinfo.server.csp.ContentSecurityModule;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.ServerDatabaseModule;
import org.activityinfo.server.database.hibernate.HibernateModule;
import org.activityinfo.server.digest.DigestModule;
import org.activityinfo.server.endpoint.content.ContentModule;
import org.activityinfo.server.endpoint.export.ExportModule;
import org.activityinfo.server.endpoint.gwtrpc.GwtRpcModule;
import org.activityinfo.server.endpoint.jsonrpc.JsonRpcModule;
import org.activityinfo.server.endpoint.odk.OdkModule;
import org.activityinfo.server.endpoint.rest.RestApiModule;
import org.activityinfo.server.generated.GeneratedModule;
import org.activityinfo.server.geo.GeometryModule;
import org.activityinfo.server.job.JobModule;
import org.activityinfo.server.login.LoginModule;
import org.activityinfo.server.mail.MailModule;
import org.activityinfo.server.report.ReportModule;
import org.activityinfo.server.util.ObjectifyModule;
import org.activityinfo.server.util.TemplateModule;
import org.activityinfo.server.util.config.ConfigModule;
import org.activityinfo.server.util.jaxrs.JaxRsModule;
import org.activityinfo.server.util.locale.LocaleModule;

import javax.servlet.ServletContextEvent;
import java.util.logging.Logger;

/**
 * A Servlet context listener that initializes the Dependency Injection
 * Framework (Guice) upon startup.
 */
public class StartupListener extends GuiceServletContextListener {

    private static Logger logger = Logger.getLogger(StartupListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("ActivityInfo servlet context is initializing");
        super.contextInitialized(servletContextEvent);

    }

    @Override
    protected Injector getInjector() {

        return Guice.createInjector(
                new HibernateModule(),
                new ConfigModule(),
                new TemplateModule(),
                new MailModule(),
                new ObjectifyModule(),
                new ServerDatabaseModule(),
                new ContentModule(),
                new GeometryModule(),
                new AuthenticationModule(),
                new ContentSecurityModule(),
                new AttachmentModule(),
                new ReportModule(),
                new DigestModule(),
                new LoginModule(),
                new GwtRpcModule(),
                new JsonRpcModule(),
                new ExportModule(),
                new JobModule(),
                new GeneratedModule(),
                new LocaleModule(),
                new JaxRsModule(),
                new RestApiModule(),
                new OdkModule(),
                new GcsBlobFieldStorageServiceModule(),
                new ApprovalModule(),
                new DatabaseModule());
    }

}