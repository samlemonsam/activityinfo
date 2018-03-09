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
package org.activityinfo.ui.client;

import com.google.common.base.Strings;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window;

public final class ClientContext {

    private static final Dictionary DICTIONARY = Dictionary
            .getDictionary("ClientContext");

    private ClientContext() {
    }

    /**
     * @return the display name of the loaded version (e.g. "2.5.6")
     */
    public static String getVersion() {
        return DICTIONARY.get("version");
    }

    public static String getAppTitle() {
        return DICTIONARY.get("title");
    }

    /**
     * @return the git commit id of the loaded version
     */
    public static String getCommitId() {
        return DICTIONARY.get("commitId");
    }

    public static String getFeatureFlags() {
        return Strings.nullToEmpty(DICTIONARY.get("featureFlags"));
    }


    public static boolean isNewFieldsFlagEnabled() {
        return getFeatureFlags().contains("newfields") ||
                Window.Location.getHostName().contains("ai-staging") ||
                Window.Location.getHostName().contains("ai-dev");
    }
}
