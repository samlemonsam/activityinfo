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

import com.google.common.base.Strings;

import java.util.Properties;

/**
 * Configuration properties for the ActivityInfo server that are specified via a
 * property file at deployment time. See {@link ConfigModule} for how these
 * properties files are located.
 */
public class DeploymentConfiguration {

    public static final String BLOBSERVICE_GCS_BUCKET_NAME = "blobservice.gcs.bucket.name";
    public static final String SERVICE_ACCOUNT_EMAIL = "service.account.email";

    private final Properties properties;

    public DeploymentConfiguration(Properties properties) {
        super();
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * @return a copy of the deployment configuration as a java.util.Properties
     * object
     */
    public Properties asProperties() {
        return (Properties) properties.clone();
    }


    public boolean hasProperty(String key) {
        return !Strings.isNullOrEmpty(getProperty(key));
    }

    public String getBlobServiceBucketName() {
        return getProperty(BLOBSERVICE_GCS_BUCKET_NAME);
    }

    public String getServiceAccountEmail() {
        return getProperty(SERVICE_ACCOUNT_EMAIL);
    }


    public int getIntProperty(String key, int defaultValue) {
        if(hasProperty(key)) {
            String stringValue = getProperty(key);
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                throw new IllegalStateException(String.format("Expected integer value for property '%s', found '%s'",
                        key, stringValue));
            }
        } else {
            return defaultValue;
        }
    }
}
