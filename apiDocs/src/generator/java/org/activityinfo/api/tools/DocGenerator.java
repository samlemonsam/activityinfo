package org.activityinfo.api.tools;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generates static API documentation
 */
public class DocGenerator {
    
    public static void main(String[] args) throws IOException, TemplateException {


        File specFile;
        File outputDir;
        if(args.length == 2) {
            specFile = new File(args[0]);
            outputDir = new File(args[1]);
        } else {
            specFile = new File("api/build/api.json");
            outputDir = new File("apiDocs/build/html");
        }

        if(!specFile.exists()) {
            System.err.println("Input file " + specFile.getAbsolutePath() + " does not exist.");
            System.exit(-1);
        }
        
        if(!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if(!created) {
                throw new IOException("Could not create " + outputDir.getAbsolutePath());
            }
        }

        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(DocGenerator.class, "/");

        Template template = configuration.getTemplate("slate.ftl");

        DocModel model = new DocModel();
        model.setTopics(renderTopics());
        model.setSpec(loadSpecModel(specFile));
        model.setLanguages("shell", "R");
        
        try(FileWriter writer = new FileWriter(new File(outputDir, "index.html"))) {
            template.process(model, writer);
        }
    }

    private static String renderTopics() throws IOException {
        File contentDir = new File("apiDocs/src/main/content");
        if(!contentDir.exists()) {
            throw new RuntimeException("Content directory does not exist: " + contentDir.getAbsolutePath());
        }
        List<File> files = Arrays.asList(contentDir.listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        StringBuilder html = new StringBuilder();
        for (File file : files) {
            PegDownProcessor processor = new PegDownProcessor(Extensions.TABLES);
            RootNode rootNode = processor.parseMarkdown(Files.asCharSource(file, Charsets.UTF_8).read().toCharArray());
            MyHtmlSerializer serializer = new MyHtmlSerializer();
            html.append(serializer.toHtml(rootNode));
        }
        return html.toString();
    }

    private static SpecModel loadSpecModel(File specFile) {
        Swagger spec = new SwaggerParser().read(specFile.getAbsolutePath());
        return new SpecModel(spec);
    }

}
