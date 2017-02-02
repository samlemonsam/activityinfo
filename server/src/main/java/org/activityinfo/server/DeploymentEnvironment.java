package org.activityinfo.server;

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

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Strings;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeploymentEnvironment {

    private static final Logger LOGGER = Logger.getLogger(DeploymentEnvironment.class.getName());

    private static Properties buildProperties = null;

    public static boolean isAppEngine() {
        return !Strings.isNullOrEmpty(SystemProperty.applicationId.get());
    }

    public static boolean isAppEngineProduction() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
    }

    public static boolean isAppEngineDevelopment() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

    private static Properties getBuildProperties() {
        if(buildProperties == null) {
            try(InputStream in = DeploymentEnvironment.class.getResourceAsStream("/org/activityinfo/server/build.properties")) {
                Properties properties = new Properties();
                properties.load(in);
                buildProperties = properties;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load build properties", e);
                buildProperties = new Properties();
            }
        }
        return buildProperties;
    }

    /**
     * @return this build's display version, for example "2.13.323", or "dev" if the build.properties
     * resource cannot be loaded.
     */
    public static String getDisplayVersion() {
        return getBuildProperties().getProperty("display.version", "dev");
    }

    /**
     *
     * @return this build's git commit sha1, or "dev" if the build.properties resource cannot be loaded.
     */
    public static String getCommitId() {
        return getBuildProperties().getProperty("commit.id", "dev");
    }
}
