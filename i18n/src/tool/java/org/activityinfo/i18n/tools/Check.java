package org.activityinfo.i18n.tools;

import com.google.common.collect.Maps;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.util.Map;


public class Check {
    
    
    private boolean failed = false;

    public Check() {
    
    }
    
    public void execute() {

        verifyTermKeysAreGloballyUnique();
        
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
