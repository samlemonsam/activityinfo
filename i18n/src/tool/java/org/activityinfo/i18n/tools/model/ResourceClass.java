package org.activityinfo.i18n.tools.model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.base.Charsets;
import org.activityinfo.i18n.tools.parser.InspectingVisitor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Describes an interface extending {@code com.google.gwt.i18n.client.Constants} or
 * {@code com.google.gwt.i18n.client.Messages}
 */
public class ResourceClass {
    
    private String sourceRoot;
    private String resourceRoot;
    private String packageName;
    private String className;

    public ResourceClass(String sourceRoot, String className) {
        this.sourceRoot = sourceRoot;
        this.resourceRoot = sourceRoot.replace("/java", "/resources");
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
        return resourceRoot + "/" + packageName.replace('.', '/') + "/" + 
                className + "_" + locale + ".properties";
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

    public Map<String, String> readResource(String language) throws IOException {
        Properties properties = new Properties();
        File file = getResourceFile(language);
        if(file.exists()) {
            try(Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                properties.load(reader);
            }
        }
        Map<String, String> map = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return map;
    }
}
