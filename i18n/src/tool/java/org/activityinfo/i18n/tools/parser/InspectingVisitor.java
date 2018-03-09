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
package org.activityinfo.i18n.tools.parser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Maps;
import org.activityinfo.i18n.tools.model.ResourceClassTerm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Finds all the keys declared in a Constants or Messages class
 */
public class InspectingVisitor extends VoidVisitorAdapter<Void> {
    
    private String filename;
    private Map<String, ResourceClassTerm> termMap = Maps.newHashMap();
    private boolean messageSubtype = false;
    private String defaultValueAnnotation = "DefaultStringValue";

    public InspectingVisitor(String filename) {
        this.filename = filename;
    }

    public Set<String> getKeys() {
        return termMap.keySet();
    }
    
    public Collection<ResourceClassTerm> getTerms() {
        return termMap.values();
    }

    public boolean isMessageSubtype() {
        return messageSubtype;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        if(AstEvaluator.isMessages(n)) {
            messageSubtype = true;
            defaultValueAnnotation = "DefaultMessage";
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        ResourceClassTerm term = new ResourceClassTerm(AstEvaluator.parseTermKey(n));

        for (AnnotationExpr annotation : n.getAnnotations()) {
            if(annotation.getName().getName().equals(defaultValueAnnotation)) {
                term.setDefaultTranslation(AstEvaluator.parseAnnotationValue(annotation));
                
            } else if(annotation.getName().getName().equals("Meaning")) {
                term.setMeaning(AstEvaluator.parseAnnotationValue(annotation));
            }
        }
        
        if(termMap.containsKey(term.getKey())) {
            throw new RuntimeException("Duplicate key: " + term.getKey());
        }
        
        termMap.put(term.getKey(), term);
    }

    public ResourceClassTerm getTerm(String key) {
        return termMap.get(key);
    }
}
