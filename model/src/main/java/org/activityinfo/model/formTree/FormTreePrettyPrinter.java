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
package org.activityinfo.model.formTree;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Dumps a FormTree to the console for debugging purposes
 */
@GwtIncompatible
public class FormTreePrettyPrinter {

    private PrintWriter pw;

    public FormTreePrettyPrinter() {
        pw = new PrintWriter(System.out);
    }

    public FormTreePrettyPrinter(PrintWriter pw) {
        this.pw = pw;
    }
    

    public void prettyPrintNodes(int indent, List<FormTree.Node> nodes) {

        List<FormClass> formClasses = distinctFormClasses(nodes);
        pw.println(classNode(formClasses));

        for(FormClass formClass : formClasses) {
            if(formClasses.size() > 1) {
                println(indent, classNode(Collections.singletonList(formClass)));
            }
            printFields(indent+1, nodes, formClass.getId());
        }

        pw.flush();
    }

    private void printFields(int indent, List<FormTree.Node> nodes, ResourceId formClassId) {

        // print data fields first
        for(FormTree.Node node : nodes) {
            if(!node.isReference() && node.getDefiningFormClass().getId().equals(formClassId)) {
                println(indent, "." + node.getField().getLabel());
            }
        }

        for(FormTree.Node node : nodes) {
            if(node.isReference() && node.getDefiningFormClass().getId().equals(formClassId)) {
                String fieldLabel = "." + node.getField().getLabel() + " = ";
                print(indent, fieldLabel);
                prettyPrintNodes(indent + fieldLabel.length(), node.getChildren());
            }
        }
    }

    private void println(int indent, String s) {
        print(indent, s);
        pw.println();
    }

    private void print(int indent, String line) {
        pw.print(Strings.repeat(".", indent));
        pw.print(line);
    }

    private List<FormClass> distinctFormClasses(List<FormTree.Node> nodes) {
        Set<ResourceId> formClassIds = Sets.newHashSet();
        List<FormClass> formClasses = Lists.newArrayList();

        for(FormTree.Node node : nodes) {
            ResourceId formClassId = node.getDefiningFormClass().getId();
            if(!formClassIds.contains(formClassId)) {
                formClassIds.add(formClassId);
                formClasses.add(node.getDefiningFormClass());
            }
        }
        return formClasses;
    }
    
    private String classNode(List<FormClass> formClasses) {

        StringBuilder sb = new StringBuilder("[");
        boolean needsPipe = false;
        for(FormClass formClass : formClasses) {
            if(needsPipe) {
                sb.append(" | ");
            }
            sb.append(formClass.getLabel());
            needsPipe = true;
        }
        sb.append("]");
        return sb.toString();
    }

    public static void print(FormTree formTree) {
        new FormTreePrettyPrinter()
                .prettyPrintNodes(0, formTree.getRootFields());
    }
    
    public void printTree(FormTree tree) {
        prettyPrintNodes(0, tree.getRootFields());
        pw.flush();
    }
}
