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
import java.util.Map;

public class Jackson {

    static JsonGenerator createJsonFactory(StringWriter writer) throws IOException {
        JsonFactory jfactory = new JsonFactory();
        JsonGenerator json = jfactory.createJsonGenerator(writer);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        json.setPrettyPrinter(prettyPrinter);
        return json;
    }

    public static String asJson(Object object) throws IOException {
        final ObjectMapper mapper = createJsonMapper().configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        return mapper.writeValueAsString(object);
    }

    public static String asJsonSilently(Object object) {
        try {
            return asJson(object);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Pretty json representation of object.
     *
     * @param object object to represent
     * @return json as string
     * @throws IOException
     */
    public static String asPrettyJson(Object object) throws IOException {
        final ObjectMapper mapper = createJsonMapper().configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        final ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
        return writer.writeValueAsString(object);
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

    public static void writeMap(JsonGenerator json, String fieldName, Map<String, Object> mapValue) throws IOException {
        json.writeObjectFieldStart(fieldName);
        for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Boolean) {
                json.writeBooleanField(entry.getKey(), (Boolean) value);
            } else if (value instanceof Double) {
                json.writeNumberField(entry.getKey(), (Double) value);
            }
        }
        json.writeEndObject();
    }

}
