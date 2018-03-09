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

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import org.activityinfo.i18n.tools.model.TranslationSet;
import org.activityinfo.i18n.tools.output.MessageDecorator;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Updates the {@code @DefaultStringValue} annotations and Javadoc in an
 * {@code com.google.gwt.i18n.client.Constants} or {@code com.google.gwt.i18n.client.Messages}
 * with the latest translations from the web-based editor
 */
public class DefaultUpdatingVisitor extends VoidVisitorAdapter<TranslationSet> {

    private boolean dirty;
    private String defaultAnnotationName;
    private Function<String, String> translationDecorator = Functions.identity();

    public DefaultUpdatingVisitor() {
        defaultAnnotationName = "DefaultStringValue";
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, TranslationSet arg) {
        if(AstEvaluator.isMessages(n)) {
            defaultAnnotationName = "DefaultMessage";
            translationDecorator = new MessageDecorator();
        }
        
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration decl, TranslationSet translations) {
        String translated = translations.get(AstEvaluator.parseTermKey(decl));
        if (!Strings.isNullOrEmpty(translated)) {
            updateDefaultStringAnnotation(decl, escapeLiteral(translationDecorator.apply(translated)));
            updateJavadoc(decl, translated);
        }
        removeUnnecessaryKeyAnnotations(decl);
    }

    private void updateJavadoc(MethodDeclaration decl, String translated) {

        // Clear Javadoc comment; way too much noise
        if(decl.getComment() != null) {
            decl.setComment(null);
            dirty = true;
        }
    }

    private void updateDefaultStringAnnotation(MethodDeclaration decl, String translated) {
        for (AnnotationExpr annotation : decl.getAnnotations()) {
            if (annotation.getName().getName().equals(defaultAnnotationName)) {
                updateAnnotationValue(annotation, translated);
                return;
            }
        }
        // If there is no annotation, add one
        SingleMemberAnnotationExpr defaultExpr = new SingleMemberAnnotationExpr(
                new NameExpr(defaultAnnotationName),
                new StringLiteralExpr(translated));
        decl.getAnnotations().add(defaultExpr);
    }

    private void updateAnnotationValue(AnnotationExpr annotation, String literalValue) {
        List<Node> children = annotation.getChildrenNodes();
        if(children.size() == 2 && 
                children.get(0) instanceof NameExpr && 
                children.get(1) instanceof StringLiteralExpr) {


            StringLiteralExpr literal = (StringLiteralExpr) children.get(1);
            if(!literal.getValue().equals(literalValue)) {
                literal.setValue(literalValue);
                dirty = true;
            }
        } else if(annotation instanceof SingleMemberAnnotationExpr) {
            // Annotations can contain more complex ASTs, for example "part of a string" + "part of another string"
            // In this case, replace wholesale
            SingleMemberAnnotationExpr smae = (SingleMemberAnnotationExpr) annotation;
            smae.setMemberValue(new StringLiteralExpr(literalValue));
        } else {
            throw new RuntimeException("Expected @" + defaultAnnotationName + " to be of type " +
                    SingleMemberAnnotationExpr.class.getName() + ", found:  " + annotation.getClass().getName());
        }
    }

    /**
     * Remove {@code Key} annotations with the same name as the method.
     */
    private void removeUnnecessaryKeyAnnotations(MethodDeclaration decl) {
        ListIterator<AnnotationExpr> it = decl.getAnnotations().listIterator();
        while(it.hasNext()) {
            AnnotationExpr annotation = it.next();
            if(annotation.getName().getName().equals("Key")) {
                String keyName = tryGetAnnotationValue(annotation);        
                if(Objects.equals(keyName, decl.getName())) {
                    it.remove();
                    dirty = true;
                }
            }
        }

    }

    private String tryGetAnnotationValue(AnnotationExpr annotation) {
        
        if(annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr smae = (SingleMemberAnnotationExpr) annotation;
            if(smae.getMemberValue() instanceof StringLiteralExpr) {
                StringLiteralExpr literalExpr = (StringLiteralExpr) smae.getMemberValue();
                return literalExpr.getValue();
            }
        }
        return null;
    }


    @VisibleForTesting
    static String escapeLiteral(String text) {
        StringBuilder escaped = new StringBuilder();

        for(int i=0;i!=text.length();++i) {
            char c = text.charAt(i);
            switch(c) {
                case '\t':
                    escaped.append('\\').append('t');
                    break;
                case '\n':
                    escaped.append('\\').append('n');
                    break;
                case '\r':
                    escaped.append('\\').append('r');
                    break;
                case '\f':
                    escaped.append('\\').append('f');
                    break;
                case '"':
                    escaped.append('\\').append(c);
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
