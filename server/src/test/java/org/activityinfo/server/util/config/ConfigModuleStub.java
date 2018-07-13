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
package org.activityinfo.server.util.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.activityinfo.server.DeploymentConfiguration;

import java.util.Properties;

public class ConfigModuleStub extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    public DeploymentConfiguration provideDeploymentConfig() {
        Properties properties = new Properties();
        properties.setProperty(DeploymentConfiguration.BLOBSERVICE_GCS_BUCKET_NAME, "app_default_bucket");
        properties.setProperty(DeploymentConfiguration.POSTMARK_WEBHOOK_TOKEN, "test_token");
        return new DeploymentConfiguration(properties);
    }

}
