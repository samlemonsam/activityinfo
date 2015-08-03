package org.activityinfo.i18n.tools;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.tools.model.TranslationSource;

import java.io.File;
import java.util.List;

/**
 * Defines properties of the I18N Module
 */
public class Project {

    public static final Project INSTANCE = new Project();
    
    private List<String> resourceClasses = Lists.newArrayList();

    private File sourceDirectory;
    private String defaultLanguage;
    private List<String> languages;
    private TranslationSource translationSource;

    private Project() {
        sourceDirectory = new File("src/main/java");
        defaultLanguage = "en";
        languages = Lists.newArrayList("en", "fr");
        translationSource = new PoEditorSource();
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
        return languages;
    }

    public TranslationSource getTranslationSource() {
        return translationSource;
    }
}
