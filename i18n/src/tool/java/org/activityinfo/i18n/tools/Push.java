package org.activityinfo.i18n.tools;

import com.google.common.collect.Lists;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.io.IOException;
import java.util.List;

/**
 * Pushes new translation keys to PoEditor
 */
public class Push {

    /**
     * If true, unused terms will be purged from PoEditor.com
     */
    private boolean purge;

    public static void main(String[] args) throws IOException {
        Push task = new Push();
        
        if(args.length > 0 && args[0].equals("purge")) {
            task.purge = true;
        }
        
        task.execute();
    }

    public void execute() throws IOException {

        List<ResourceClassTerm> terms = Lists.newArrayList();
        
        for(String className : Project.INSTANCE.getResourceClasses()) {
            ResourceClass resourceClass = new ResourceClass(Project.INSTANCE.getSourceDirectory(), className);
            InspectingVisitor visitor = resourceClass.inspect();
            terms.addAll(visitor.getTerms());
        }

       Project.INSTANCE.getTranslationSource().updateTerms(terms, purge);
    }
}
