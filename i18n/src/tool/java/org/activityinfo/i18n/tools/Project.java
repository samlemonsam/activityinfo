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
        translationSource = new PoEditorSource(ACTIVITYINFO_PROJECT_ID, System.getProperty("poApiKey"));
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
        return translationSource;
    }
}
