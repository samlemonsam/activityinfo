package org.activityinfo.server.util.jaxrs;

import com.bedatadriven.geojson.GeoJsonModule;
import org.activityinfo.store.server.JaxRsJsonReader;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


/**
 * Wraps the JacksonJsonProvider to refine the ObjectMapper and
 * to ensure that the Content-Type header always includes the
 * charset=UTF-8 fragment
 */
public class Utf8JacksonJsonProvider extends JacksonJsonProvider {

    private JaxRsJsonReader ourJsonReader = new JaxRsJsonReader();

    public Utf8JacksonJsonProvider() {
        super(createObjectMapper());
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new GeoJsonModule());
        return mapper;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ourJsonReader.isReadable(type, genericType, annotations, mediaType) ||
                super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ourJsonReader.isWriteable(type, genericType, annotations, mediaType) ||
                super.isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(Object value,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {

        httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaType.toString() + ";charset=UTF-8");

        if(ourJsonReader.isWriteable(type, genericType, annotations, mediaType)) {
            ourJsonReader.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
            return;
        }

        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

        if(ourJsonReader.isReadable(type, genericType, annotations, mediaType)) {
            return ourJsonReader.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
        }

        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
