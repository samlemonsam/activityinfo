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
package org.activityinfo.i18n.tools;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.shared.ApplicationLocale;

import java.io.File;
import java.util.List;

/**
 * Defines properties of the I18N Module
 */
public class Project {

    public static final Project INSTANCE = new Project();
    public static final int ACTIVITYINFO_PROJECT_ID = 26801;

    private List<String> resourceClasses = Lists.newArrayList();

    private File sourceDirectory;
    private String defaultLanguage;
    private PoEditorSource translationSource;

    private Project() {
        sourceDirectory = new File("src/main/java");
        defaultLanguage = "en";
        resourceClasses.add("org.activityinfo.i18n.shared.UiConstants");
        resourceClasses.add("org.activityinfo.i18n.shared.UiMessages");
    }

    public List<String> getResourceClasses() {
        return resourceClasses;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public List<String> getLanguages() {
        List<String> languageCodes = Lists.newArrayList();
        for (ApplicationLocale applicationLocale : ApplicationLocale.values()) {
            languageCodes.add(applicationLocale.name().toLowerCase());
        }
        return languageCodes;
    }

    public PoEditorSource getTranslationSource() {
        if(translationSource == null) {
            translationSource = new PoEditorSource(ACTIVITYINFO_PROJECT_ID, System.getProperty("poApiKey"));
        }
        return translationSource;
    }
}
