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

    private boolean dryRun;

    public static void main(String[] args) throws IOException {
        Push task = new Push();

        for (String arg : args) {
            if("purge".equals(arg)) {
                task.purge = true;
            } else if("dryRun".equalsIgnoreCase(arg)) {
                task.dryRun = true;
            }
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

        if(dryRun) {
            Project.INSTANCE.getTranslationSource().dumpNewTerms(terms);
        } else {
            Project.INSTANCE.getTranslationSource().updateTerms(terms, purge);
        }
    }
}
