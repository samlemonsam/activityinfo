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
