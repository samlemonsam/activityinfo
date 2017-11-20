package org.activityinfo.i18n.tools;


import com.github.javaparser.ast.CompilationUnit;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.PropertiesBuilder;
import org.activityinfo.i18n.tools.parser.DefaultUpdatingVisitor;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;
import org.activityinfo.i18n.tools.parser.ValidatingVisitor;

import java.io.File;
import java.io.IOException;


/**
 * Retrieves translations from PoEditor
 */
public class Pull {


    public static void main(String[] args) throws IOException {
        Pull task = new Pull();
        task.execute();
    }
    
    public void execute() throws IOException {

        for(String language : Project.INSTANCE.getLanguages()) {
            for(String className : Project.INSTANCE.getResourceClasses()) {
                updateResourceClass(className, fetchTranslations(language));
            }
        }
    }

    @VisibleForTesting
    void updateResourceClass(String resourceClassName, TranslationSet translations) throws IOException {
        ResourceClass resourceClass = new ResourceClass(Project.INSTANCE.getSourceDirectory().getAbsolutePath(), resourceClassName);
        if(!resourceClass.getJavaSourceFile().exists()) {
            throw new IOException(
                    "Resource class " + resourceClassName + " does not exist at " + resourceClass.getJavaSourcePath());
        }

        if(translations.getLanguage().equals(Project.INSTANCE.getDefaultLanguage())) {
            updateJavaSource(resourceClass, translations);

        } else {
            updateProperties(resourceClass, translations);
        }
    }

    /**
     * Fetches the latest translations from the web-based editor
     */
    private TranslationSet fetchTranslations(String language) throws IOException {
        TranslationSet set = Project.INSTANCE.getTranslationSource().fetchTranslations(language);
       
        if(set.isEmpty()) {
            throw new IOException(
                    String.format("No %s translations available", language));
        }

        return set;

    }

    TranslationSet validateMessages(ResourceClass resourceClass, TranslationSet translationSet) throws IOException {
        CompilationUnit cu = null;
        try {
            cu = resourceClass.parseJavaSource();
        } catch (Exception e) {
            throw new IOException("Exception parsing " + resourceClass.getJavaSourcePath(), e);
        }

        return validateMessages(resourceClass, cu, translationSet);
    }

    private TranslationSet validateMessages(ResourceClass resourceClass, CompilationUnit cu, TranslationSet translationSet) throws IOException {
        InspectingVisitor inspector = new InspectingVisitor(resourceClass.getJavaSourceFile().getName());
        inspector.visit(cu, null);

        for (ResourceClassTerm resourceClassTerm : inspector.getTerms()) {
            checkForNewline(resourceClassTerm, translationSet);
        }

        if(!inspector.isMessageSubtype()) {
            return translationSet;
        }

        ValidatingVisitor validator = new ValidatingVisitor(translationSet);
        validator.visit(cu, null);

        return validator.getValidatedSet();
    }

    private void checkForNewline(ResourceClassTerm term, TranslationSet translationSet) throws IOException {
        if (checkForNewline(term.getDefaultTranslation())) {
            throw new IOException(String.format("Default string %s: '%s' contains illegal newline",
                    term.getKey(), term.getDefaultTranslation()));
        }
        if (checkForNewline(translationSet.get(term.getKey()))) {
            throw new IOException(String.format("Translated string %s[%s] contains illegal newline",
                    term.getKey(), translationSet.getLanguage()));
        }
    }

    private boolean checkForNewline(String string) {
        return string != null && string.contains("\n");
    }

    /**
     * Updates the @DefaultMessage/@DefaultStringValue annotations and javadoc in the Messages or Constants
     * interface Java source file.
     */
    private void updateJavaSource(ResourceClass resourceClass, TranslationSet translations) throws IOException {
        CompilationUnit compilationUnit;
        try {
            compilationUnit = resourceClass.parseJavaSource();
        } catch (Exception e) {
            throw new IOException("Failed to parse " + resourceClass.getJavaSourcePath(), e);
        }

        TranslationSet validated = validateMessages(resourceClass, compilationUnit, translations);

        DefaultUpdatingVisitor visitor = new DefaultUpdatingVisitor();
        visitor.visit(compilationUnit, validated);

        if(visitor.isDirty()) {
            try {
                Files.write(compilationUnit.toString(), resourceClass.getJavaSourceFile(), Charsets.UTF_8);
            } catch (IOException e) {
                throw new IOException("Failed to write updated source file to " +
                        resourceClass.getJavaSourcePath(), e);
            }
            System.out.println("Updated default translations in " + resourceClass.getJavaSourcePath());
        } else {
            System.out.println(resourceClass.getClassName() + " is up to date.");
        }
    }

    /**
     * Updates the properties files with the latest translations
     */
    private void updateProperties(ResourceClass resourceClass, TranslationSet translations) throws IOException {

        // Validate messages first to avoid causing compile errors
        TranslationSet validated = validateMessages(resourceClass, translations);


        // Write out the properties file
        PropertiesBuilder properties = new PropertiesBuilder();
        properties.addAll(resourceClass, validated);

        File resourceFile = resourceClass.getResourceFile(translations.getLanguage());

        if(properties.getMissingCount() > 0) {
            System.err.println(resourceFile.getName() + " is missing " + properties.getMissingCount() + " translations.");
        }

        Files.write(properties.toString(), resourceFile, Charsets.UTF_8);

        System.err.println("Updated " + resourceFile.getName());
    }
}