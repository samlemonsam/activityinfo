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
