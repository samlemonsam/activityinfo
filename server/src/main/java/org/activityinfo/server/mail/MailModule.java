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
package org.activityinfo.server.mail;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.util.jaxrs.AbstractRestModule;

public class MailModule extends AbstractRestModule {


    @Override
    protected void configureResources() {
        bindResource(BounceHook.class, "/postmark/*");
    }

    @Provides @Singleton
    public MailSender provideMailSender(DeploymentConfiguration config, Injector injector) {
        if (config.hasProperty("postmark.key")) {
            return injector.getInstance(PostmarkMailSender.class);
        } else {
            return injector.getInstance(SmtpMailSender.class);
        }

    }
}
