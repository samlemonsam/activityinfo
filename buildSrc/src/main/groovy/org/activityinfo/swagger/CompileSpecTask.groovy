package org.activityinfo.swagger

import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class CompileSpecTask extends DefaultTask {

    @InputFile
    File yamlFile

    @OutputFile
    File outputFile

    @TaskAction
    def convert() {
        def spec = parseYaml(yamlFile);
        outputFile.write(new JsonBuilder(spec).toPrettyString())
    }

    def parseYaml(File file) {
        def yaml = new org.yaml.snakeyaml.Yaml();
        file.withReader {
            try {
                return yaml.load(it);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse YAML file ${file.name}: ${e.message}", e);
            }
        }
    }
}
