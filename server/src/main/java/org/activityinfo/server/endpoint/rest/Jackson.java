package org.activityinfo.server.endpoint.rest;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.IOException;
import java.io.StringWriter;

public class Jackson {

    static JsonGenerator createJsonFactory(StringWriter writer) throws IOException {
        JsonFactory jfactory = new JsonFactory();
        JsonGenerator json = jfactory.createJsonGenerator(writer);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        json.setPrettyPrinter(prettyPrinter);
        return json;
    }

    /**
     * Creates json mapper.
     *
     * @return json mapper
     */
    public static ObjectMapper createJsonMapper() {
        final AnnotationIntrospector jaxb = new JaxbAnnotationIntrospector();
        final AnnotationIntrospector jackson = new JacksonAnnotationIntrospector();

        final AnnotationIntrospector pair = new AnnotationIntrospector.Pair(jackson, jaxb);

        final ObjectMapper mapper = new ObjectMapper();
        mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
        mapper.getSerializationConfig().withAnnotationIntrospector(pair);
        return mapper;
    }

}
