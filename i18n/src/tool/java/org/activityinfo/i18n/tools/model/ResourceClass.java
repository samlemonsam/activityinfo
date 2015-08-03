package org.activityinfo.i18n.tools.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.io.File;
import java.io.IOException;

/**
 * Describes an interface extending {@code com.google.gwt.i18n.client.Constants} or
 * {@code com.google.gwt.i18n.client.Messages}
 */
public class ResourceClass {
    
    private String sourceRoot;
    private String packageName;
    private String className;

    public ResourceClass(String sourceRoot, String className) {
        this.sourceRoot = sourceRoot;
        int lastDot = className.lastIndexOf('.');
        this.packageName = className.substring(0, lastDot);
        this.className = className.substring(lastDot+1);
    }

    public ResourceClass(File sourceRoot, String className) {
        this(sourceRoot.getAbsolutePath(), className);
    }

    public String getJavaSourcePath() {
        return getPackagePath() + "/" + className + ".java";
    }

    private String getPackagePath() {
        return sourceRoot + "/" + packageName.replace('.', '/');
    }

    public File getJavaSourceFile() {
        return new File(getJavaSourcePath());
    }
    
    public String getResourcePath(String locale) {
        return getPackagePath() + "/" + className + "_" + locale + ".properties";
    }

    public File getResourceFile(String language) {
        return new File(getResourcePath(language));
    }

    public CompilationUnit parseJavaSource() throws IOException, ParseException {
        return JavaParser.parse(getJavaSourceFile());
    }

    public String getClassName() {
        return packageName + "." + className;
    }
    
    public InspectingVisitor inspect() {
        // Extract keys from the resource file
        InspectingVisitor visitor = new InspectingVisitor(getJavaSourceFile().getName());
        try {
            CompilationUnit cu = parseJavaSource();
            visitor.visit(cu, null);
        } catch (Exception e) {
            throw new RuntimeException("Exception parsing " + getJavaSourceFile() +
                ": " + e.getMessage(), e);
        }

        return visitor;
    }

}
