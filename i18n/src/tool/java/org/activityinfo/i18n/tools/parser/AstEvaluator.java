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
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.common.annotations.VisibleForTesting;


public class AstEvaluator {
    static boolean isMessages(ClassOrInterfaceDeclaration decl) {
        for (ClassOrInterfaceType superType : decl.getExtends()) {
            if(superType.getName().contains("Messages")) {
                return true;
            }
        }
        return false;
    }

    static String parseTermKey(MethodDeclaration decl) {
        for(AnnotationExpr annotationExpr : decl.getAnnotations()) {
            if(annotationExpr.getName().getName().equals("Key")) {
                return parseAnnotationValue(annotationExpr);
            }
        }
        return decl.getName();
    }

    @VisibleForTesting
    static String parseAnnotationValue(AnnotationExpr annotation) {

        if(annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr smae = (SingleMemberAnnotationExpr) annotation;
            return parseStringLiteral(smae.getMemberValue());
        }
        throw new RuntimeException(String.format(
                "Cannot parse the annotation value at line %d: %s. Please use a single string literal.",
                annotation.getBeginLine(), annotation.toString()));
    }

    private static String parseStringLiteral(Expression expr) {
        if (expr instanceof StringLiteralExpr) {
            return unescape(((StringLiteralExpr) expr).getValue());
        } else if(expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            if(binaryExpr.getOperator() == BinaryExpr.Operator.plus) {
                return parseStringLiteral(binaryExpr.getLeft()) + parseStringLiteral(binaryExpr.getRight());
            } else {
                throw new UnsupportedOperationException("Operator: " + binaryExpr.getOperator());
            }
        } else {
            throw new UnsupportedOperationException("Don't know how to evaluate " + expr);
        }
    }

    private static String unescape(String in) {

        StringBuilder out = new StringBuilder();
        int i=0;
        while(i < in.length()) {
            char c = in.charAt(i++);
            if(c == '\\') {
                if(i == in.length()) {
                    throw new RuntimeException("Unexpected end of string literal, expected code to follow '\\'");
                }
                char escapeCode = in.charAt(i++);
                switch(escapeCode) {
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case '"':
                    case '\\':
                        out.append(escapeCode);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported escape sequence starting at '" + escapeCode + "'");
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }


}
