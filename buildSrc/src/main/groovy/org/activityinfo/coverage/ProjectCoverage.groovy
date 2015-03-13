package org.activityinfo.coverage

import groovy.xml.MarkupBuilder
import org.gradle.api.Project

/**
 * Model classes for combining GWT and jacoco test coverage results
 */
class ProjectCoverage {
    
    Map<String, FileCoverage> files = new HashMap<>()
    Map<String, String> pathMap = new HashMap<>();

    public ProjectCoverage(Project project) {
        project.subprojects.each { p ->
            def sourceSet = p.sourceSets?.main?.allJava
            if(sourceSet) {
                sourceSet.visit { f ->
                    if(f.name.endsWith(".java")) {
                        def classPath = f.relativePath.toString()
                        def projectPath = project.relativePath(f.file.absolutePath).toString()
                        pathMap.put(classPath, projectPath)
                    }
                }
            }
        }
    }
    
    public FileCoverage getSource(String sourceFile) {
        def projectRelativePath = pathMap.get(sourceFile)
        if(projectRelativePath == null) {
            throw new IllegalArgumentException("[${sourceFile}] cannot be found")
        }
        FileCoverage fileCoverage = files.get(sourceFile)
        if(!fileCoverage) {
            fileCoverage = new FileCoverage(projectRelativePath)
            files.put(sourceFile, fileCoverage)
        }
        return fileCoverage
    }
   
    
    public void writeReport(File reportFile) {

        // Write out the XML Summary
        reportFile.withWriter { writer ->
            def xml = new MarkupBuilder(writer)
            xml.coverage(version: 1) {
                files.each { path, coverage ->
                    xml.file(path: coverage.path) {
                        coverage.lineMap.each { lineNumber, covered ->
                            lineToCover(lineNumber: lineNumber, covered: covered)
                        }
                    }
                }
            }
        }

    }
}
