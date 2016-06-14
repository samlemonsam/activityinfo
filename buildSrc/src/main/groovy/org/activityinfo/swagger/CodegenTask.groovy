package org.activityinfo.swagger

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CodegenTask extends DefaultTask {

    @Input
    File inputFile;
    
    @Input
    String language;
    
    @Input
    File configFile;
    
    @OutputDirectory
    File outputDir
    
    @TaskAction
    def generate() {
        project.javaexec {
            main = 'io.swagger.codegen.SwaggerCodegen'
            args 'generate'
            args '-i', inputFile.absolutePath
            args '-l', language

            if (configFile != null) {
                args '-c', configFile.absolutePath
            }

            args '-o', outputDir.absolutePath
        }


        
    }
}
