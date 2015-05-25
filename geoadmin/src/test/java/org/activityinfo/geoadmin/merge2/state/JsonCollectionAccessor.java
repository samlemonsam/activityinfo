package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class JsonCollectionAccessor implements CollectionAccessor {
    
    private int id;
    private int parentId;
    private String name;
    
    private final FormClass formClass;
    private final JsonArray instances;
    
    public JsonCollectionAccessor(String resourceName) throws IOException {
        formClass = loadFormClass(resourceName);
        instances = loadInstances(resourceName);
    }

    private FormClass loadFormClass(String resourceName) throws IOException {
        String json = getJson(resourceName + "/form.json").read();
        Resource resource = org.activityinfo.model.resource.Resources.fromJson(json);
        return FormClass.fromResource(resource);
    }

    private JsonArray loadInstances(String resourceName) throws IOException {
        Gson gson = new Gson();
        try(Reader reader = getJson(resourceName + "/instances.json").openStream()) {
            return gson.fromJson(reader, JsonArray.class);
        } catch (Exception e) {
            throw new IOException("Exception loading instances for " + resourceName, e);
        }
    }

    private CharSource getJson(String resourceName) throws IOException {
        URL classUrl = Resources.getResource(resourceName);
        return Resources.asCharSource(classUrl, Charsets.UTF_8);
    }
    
    private FormField getField(ResourceId id) {
        for (FormField formField : formClass.getFields()) {
            if(formField.getId().equals(id)) {
                return formField;
            }
        }
        throw new IllegalArgumentException(id.toString());
    }

    
    @Override
    public Resource get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
         return formClass;
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new JsonQueryBuilder();
    }

    private class JsonQueryBuilder implements ColumnQueryBuilder {
        
        private List<CursorObserver<JsonObject>> bindings = new ArrayList<>();
        
        @Override
        public void addResourceId(CursorObserver<ResourceId> observer) {
            bindings.add(new IdBinding(observer));
        }

        @Override
        public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
            FormField field = getField(fieldId);
            if(field.getType() instanceof TextType) {
                bindings.add(new TextFieldBinding(fieldId.asString(), observer));
            } else if(field.getType() instanceof ReferenceType) {
                bindings.add(new ReferenceFieldBinding(fieldId.asString(), observer));
            } else {
                throw new UnsupportedOperationException("type: " + field.getType());
            }
        }

        @Override
        public void execute() throws IOException {
            for(int i=0;i<instances.size();++i) {
                JsonObject instance = instances.get(i).getAsJsonObject();
                for (CursorObserver<JsonObject> binding : bindings) {
                    binding.onNext(instance);
                }
            }
            for (CursorObserver<JsonObject> binding : bindings) {
                binding.done();
            }
        }
    }

    private static class IdBinding implements CursorObserver<JsonObject> {
        private CursorObserver<ResourceId> observer;

        public IdBinding(CursorObserver<ResourceId> observer) {
            this.observer = observer;
        }

        @Override
        public void onNext(JsonObject instance) {
            observer.onNext(ResourceId.valueOf(instance.get("id").getAsString()));
        }

        @Override
        public void done() {
            observer.done();
        }
    }
    
    private abstract static class FieldBinding implements CursorObserver<JsonObject> {
        private String field;
        private CursorObserver<FieldValue> observer;

        public FieldBinding(String field, CursorObserver<FieldValue> observer) {
            this.field = field;
            this.observer = observer;
        }

        @Override
        public void onNext(JsonObject instance) {
            if(instance.has(field)) {
                observer.onNext(convert(instance.get(field)));
            } else {
                observer.onNext(null);
            }
        }

        protected abstract FieldValue convert(JsonElement jsonElement);

        @Override
        public void done() {
            observer.done();
        }
    }
    
    private static class TextFieldBinding extends FieldBinding {

        public TextFieldBinding(String field, CursorObserver<FieldValue> observer) {
            super(field, observer);
        }

        @Override
        protected FieldValue convert(JsonElement jsonElement) {
            return TextValue.valueOf(jsonElement.getAsString());
        }
    }
    
    private static class ReferenceFieldBinding extends FieldBinding {

        public ReferenceFieldBinding(String field, CursorObserver<FieldValue> observer) {
            super(field, observer);
        }

        @Override
        protected FieldValue convert(JsonElement jsonElement) {
            return new ReferenceValue(ResourceId.valueOf(jsonElement.getAsString()));
        }
    }
    
}
