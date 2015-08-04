package org.activityinfo.i18n.tools;

import com.google.common.collect.Maps;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Check {
    
    
    private boolean failed = false;

    public Check() {
    
    }
    
    public void execute() {
        try {
            verifyTermKeysAreGloballyUnique();
            checkForXmlEntities();
        } catch (Exception e) {
            System.err.println("Exception while running checkMessages: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private void verifyTermKeysAreGloballyUnique()  {
        Map<String, ResourceClass> keys = Maps.newHashMap();

        for(String className : Project.INSTANCE.getResourceClasses()) {
            ResourceClass resourceClass = new ResourceClass(Project.INSTANCE.getSourceDirectory(), className);
            InspectingVisitor visitor = resourceClass.inspect();

            for(String key : visitor.getKeys()) {
                ResourceClass previousDefinition = keys.put(key, resourceClass);
                if(previousDefinition != null) {
                    System.err.println(String.format("Duplicate term key '%s' in %s (previously defined in %s)",
                            key, resourceClass.getClassName(), previousDefinition.getClassName()));
                    failed = true;
                }
            }
        }
        System.out.println("Found " + keys.size() + " unique term keys");
    }
    
    private void checkForXmlEntities() throws IOException {

        Pattern entityPattern = Pattern.compile("&#?[A-Za-z0-9]+;");
        boolean entityFound = false;

        for(String className : Project.INSTANCE.getResourceClasses()) {
            ResourceClass resourceClass = new ResourceClass(Project.INSTANCE.getSourceDirectory(), className);
            InspectingVisitor visitor = resourceClass.inspect();

            for (ResourceClassTerm term : visitor.getTerms()) {
                Matcher matcher = entityPattern.matcher(term.getDefaultTranslation());
                if(matcher.find()) {
                    System.err.println(String.format("Term %s in %s uses the XML entity %s.", 
                            term.getKey(), className, matcher.group()));
                    entityFound = true;
                }
            }

            for (String language : Project.INSTANCE.getLanguages()) {
                File resourceFile = resourceClass.getResourceFile(language);
                Map<String, String> translations = resourceClass.readResource(language);
                for (String key : translations.keySet()) {
                    String translation = translations.get(key);
                    Matcher matcher = entityPattern.matcher(translation);
                    if(matcher.find()) {
                        System.err.println(String.format("Translation %s in %s uses XML entity %s",
                            key, resourceFile.getName(), matcher.group()));
                        entityFound = true;
                    }
                }
            }
        }
        
        if(entityFound) {
            System.err.println("XML Entities should not be used in terms because they are not parsed in many contexts.");
            System.err.println("Use unicode escapes instead.");
            failed = true;
        }
    }
    
    public static void main(String[] args) {
        Check check = new Check();
        check.execute();
        
        if(check.failed) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
    }
}
