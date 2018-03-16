/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import jsinterop.annotations.JsType;
import org.activityinfo.json.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
@Produces({MediaType.APPLICATION_JSON, "text/json"})
@GwtIncompatible
public class JaxRsJsonReader
        implements
        MessageBodyReader<Object>,
        MessageBodyWriter<Object>
{

    private ConcurrentHashMap<Class, Optional<Method>> factoryMethodCache = new ConcurrentHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(JaxRsJsonReader.class.getName());

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JsonValue.class.isAssignableFrom(type)  || isJsType(type) || getFactoryMethod(type).isPresent();
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {

        InputStreamReader reader = new InputStreamReader(entityStream, Charsets.UTF_8);
        JsonValue jsonValue = Json.parse(CharStreams.toString(reader));

        if(isJsType(type)) {
            try {
                return Json.fromJson(type, jsonValue);
            } catch (JsonMappingException e) {
                LOGGER.log(Level.WARNING, "Failed to parse JSON: ", e);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } else if(type.equals(JsonValue.class)) {
            return jsonValue;
        }

        Method factoryMethod = getFactoryMethod(type).get();
        try {
            return factoryMethod.invoke(null, jsonValue);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Failed to invoke " + factoryMethod, e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

        } catch (InvocationTargetException e) {
            LOGGER.log(Level.WARNING, "Failed to parse: " + Json.stringify(jsonValue), e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JsonValue.class.isAssignableFrom(type) || isJsType(type) || JsonSerializable.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        JsonValue jsonValue;
        if(isJsType(type)) {
            jsonValue = Json.toJson(o);
        } else if(o instanceof JsonSerializable) {
            jsonValue = ((JsonSerializable) o).toJson();
        } else if(o instanceof JsonValue) {
            jsonValue = (JsonValue) o;
        } else {
            throw new UnsupportedOperationException("o: " + o.getClass().getName());
        }

        entityStream.write(Json.stringify(jsonValue).getBytes(Charsets.UTF_8));
    }


    private Optional<Method> getFactoryMethod(Class<?> type) {

        Optional<Method> factoryMethod = factoryMethodCache.get(type);
        if(factoryMethod == null) {
            factoryMethod = findFactoryMethod(type);
            factoryMethodCache.put(type, factoryMethod);
        }
        return factoryMethod;
    }

    private Optional<Method> findFactoryMethod(Class<?> type) {

        if(type.getAnnotation(JsType.class) != null) {
            try {
                return Optional.of(Json.class.getMethod("fromJson", Class.class, JsonValue.class));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        try {
            Method factoryMethod = type.getMethod("fromJson", JsonValue.class);
            if (Modifier.isPublic(factoryMethod.getModifiers()) &&
                    Modifier.isStatic(factoryMethod.getModifiers()) &&
                    factoryMethod.getParameterTypes().length == 1 &&
                    factoryMethod.getParameterTypes()[0].equals(JsonValue.class)) {

                return Optional.of(factoryMethod);
            } else {
                LOGGER.warning("Found " + type.getName() + ".fromJson() but did not have expected signature.");
                return Optional.absent();
            }
        } catch (NoSuchMethodException e) {
            LOGGER.warning("Did not find " + type.getName() + ".fromJson()");
            return Optional.absent();
        }
    }

    private boolean isJsType(Class<?> type) {
        return type.getAnnotation(JsType.class) != null;
    }

}