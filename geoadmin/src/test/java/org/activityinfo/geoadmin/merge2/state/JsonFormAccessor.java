package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.api.client.FormHistoryEntryBuilder;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoArea;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.service.store.FormAccessor;
import org.activityinfo.service.store.FormPermissions;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class JsonFormAccessor implements FormAccessor {
    
    private int id;
    private int parentId;
    private String name;
    
    private final FormClass formClass;
    private final JsonArray instances;
    
    public JsonFormAccessor(String resourceName) throws IOException {
        formClass = loadFormClass(resourceName);
        instances = loadInstances(resourceName);
    }

    private FormClass loadFormClass(String resourceName) throws IOException {
        String json = getJson(resourceName + "/form.json").read();
        Resource resource = org.activityinfo.model.resource.Resources.resourceFromJson(json);
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
    public FormPermissions getPermissions(int userId) {
        return FormPermissions.full();
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<FormHistoryEntryBuilder> getHistory(ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
         return formClass;
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(RecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(RecordUpdate update) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new JsonQueryBuilder();
    }

    @Override
    public long cacheVersion() {
        return 0;
    }


    private class JsonQueryBuilder implements ColumnQueryBuilder {
        
        private List<CursorObserver<JsonObject>> bindings = new ArrayList<>();

   
        @Override
        public void only(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

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
            } else if(field.getType() instanceof GeoAreaType) {
                bindings.add(new GeoAreaFieldBinding(fieldId.asString(), observer));
            } else {
                throw new UnsupportedOperationException("type: " + field.getType());
            }
        }

        @Override
        public void execute() {
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
    
    private static class GeoAreaFieldBinding extends FieldBinding {

        public GeoAreaFieldBinding(String field, CursorObserver<FieldValue> observer) {
            super(field, observer);
        }

        @Override
        protected FieldValue convert(JsonElement jsonElement) {
            JsonArray array = jsonElement.getAsJsonObject().get("extents").getAsJsonArray();
            Extents extents = Extents.create(
                    array.get(0).getAsDouble(),
                    array.get(1).getAsDouble(),
                    array.get(2).getAsDouble(),
                    array.get(3).getAsDouble());
            
            return new GeoArea(extents, "");
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
