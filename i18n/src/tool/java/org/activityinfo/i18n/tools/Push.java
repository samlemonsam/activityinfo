package org.activityinfo.i18n.tools;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.activityinfo.i18n.tools.model.ResourceClass;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;
import org.activityinfo.i18n.tools.model.Term;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.PropertiesBuilder;
import org.activityinfo.i18n.tools.parser.DefaultUpdatingVisitor;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;
import org.activityinfo.i18n.tools.parser.ValidatingVisitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Pushes new translation keys to PoEditor
 */
public class Push {



    public static void main(String[] args) throws IOException {
        Push task = new Push();
        task.execute();
    }

    public void execute() throws IOException {

        Map<String, Term> existingTerms;
        existingTerms = Project.INSTANCE.getTranslationSource().fetchTerms();
       

        List<ResourceClassTerm> toAdd = Lists.newArrayList();

        for(String className : Project.INSTANCE.getResourceClasses()) {
            ResourceClass resourceClass = new ResourceClass(Project.INSTANCE.getSourceDirectory(), className);
            InspectingVisitor visitor = resourceClass.inspect();

            for(ResourceClassTerm term : visitor.getTerms()) {
                if(!existingTerms.containsKey(term.getKey())) {
                    System.out.println("New term " + term.getKey() + " = " + term.getDefaultTranslation());
                    toAdd.add(term);
                }
            }
        }

        Project.INSTANCE.getTranslationSource().addTerms(toAdd);
    }
}
