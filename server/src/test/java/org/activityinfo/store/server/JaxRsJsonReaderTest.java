package org.activityinfo.store.server;

import com.google.common.base.Charsets;
import jsinterop.annotations.JsType;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JaxRsJsonReaderTest {

    public static final MultivaluedMap EMPTY_HEADERS = null;

    @JsType
    public static class DummyJsObject {
        public boolean b;
        public int i;
        public double d;
        public String s;
        public String s0;
        public JsonValue object;
    }

    @Test
    public void jsonValue() throws IOException {

        JaxRsJsonReader reader = new JaxRsJsonReader();
        assertTrue(reader.isReadable(JsonValue.class, JsonValue.class, new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        assertTrue(reader.isWriteable(JsonValue.class, JsonValue.class, new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        JsonValue object = Json.createObject();
        object.put("b", true);
        object.put("s", "hello WOrld");
        object.put("i", 42);

        ByteArrayOutputStream entity = new ByteArrayOutputStream();
        reader.writeTo(object, object.getClass(), object.getClass(),
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS,
                entity);

        JsonValue reobject = (JsonValue) reader.readFrom((Class)JsonValue.class, JsonValue.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS,
                new ByteArrayInputStream(entity.toByteArray()));


        assertThat(reobject.getBoolean("b"), equalTo(true));
        assertThat(reobject.getNumber("i"), equalTo(42.0));
        assertThat(reobject.getString("s"), equalTo("hello WOrld"));
    }

    @Test
    public void jsTypeTest() throws IOException {

        JaxRsJsonReader reader = new JaxRsJsonReader();
        assertTrue(reader.isReadable(DummyJsObject.class, DummyJsObject.class, new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        assertTrue(reader.isWriteable(DummyJsObject.class, DummyJsObject.class, new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        DummyJsObject object = new DummyJsObject();
        object.b = true;
        object.s = "Hello world";
        object.i = 99;

        ByteArrayOutputStream entity = new ByteArrayOutputStream();
        reader.writeTo(object, object.getClass(), object.getClass(),
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS,
                entity);

        JsonValue jsonValue = Json.parse(new String(entity.toByteArray(), Charsets.UTF_8));
        assertThat(jsonValue.getString("s"), equalTo("Hello world"));


        DummyJsObject reobject = (DummyJsObject) reader.readFrom((Class)DummyJsObject.class, DummyJsObject.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS,
                new ByteArrayInputStream(entity.toByteArray()));


        assertThat(reobject.b, equalTo(object.b));
        assertThat(reobject.d, equalTo(object.d));
        assertThat(reobject.s, equalTo(object.s));
    }

    @Test
    public void modelClass() throws IOException {
        FormClass formClass = new FormClass(ResourceId.valueOf("FORM"));
        formClass.setLabel("My Form");
        formClass.addField(ResourceId.valueOf("F1"))
                .setLabel("My field")
                .setRequired(true)
                .setType(TextType.SIMPLE)
                .setCode("MY");


        JaxRsJsonReader reader = new JaxRsJsonReader();
        assertTrue(reader.isReadable(FormClass.class, FormClass.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        assertTrue(reader.isWriteable(FormClass.class, FormClass.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE));

        ByteArrayOutputStream entity = new ByteArrayOutputStream();
        reader.writeTo(formClass, formClass.getClass(), formClass.getClass(),
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS, entity);

        JsonValue jsonObject = Json.parse(new String(entity.toByteArray(), Charsets.UTF_8));
        System.out.println(Json.stringify(jsonObject));

        FormClass reformClass = (FormClass) reader.readFrom((Class)FormClass.class, FormClass.class,
                new Annotation[0],
                MediaType.APPLICATION_JSON_TYPE,
                EMPTY_HEADERS,
                new ByteArrayInputStream(entity.toByteArray()));
    }

}