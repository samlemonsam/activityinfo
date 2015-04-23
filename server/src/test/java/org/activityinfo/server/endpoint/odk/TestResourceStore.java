package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.server.command.ResourceLocatorSync;
import org.activityinfo.service.lookup.ReferenceChoice;

import java.io.IOException;
import java.net.URL;
import java.sql.Ref;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestResourceStore implements ResourceLocatorSync {

    private Optional<FormInstance> lastUpdated = Optional.absent();

    private Map<ResourceId, Resource> resources = Maps.newHashMap();

    @Override
    public void persist(FormInstance formInstance) {
        lastUpdated = Optional.of(formInstance);
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        Resource resource = resources.get(resourceId);
        if(resource == null) {
            throw new IllegalArgumentException(resourceId.asString());
        }
        return FormClass.fromResource(resource);
    }

    @Override
    public FormInstance getFormInstance(ResourceId resourceId) {
        Resource resource = resources.get(resourceId);
        if(resource == null) {
            throw new IllegalArgumentException(resourceId.asString());
        }
        return FormInstance.fromResource(resource);
    }

    @Override
    public List<ReferenceChoice> getReferenceChoices(Set<ResourceId> range) {
        List<ReferenceChoice> choices = Lists.newArrayList();
        for(Resource resource : resources.values()) {
            FormInstance instance = FormInstance.fromResource(resource);
            if(range.contains(instance.getClassId())) {
                choices.add(new ReferenceChoice(instance.getId(), FormInstanceLabeler.getLabel(instance)));
            }
        }
        return choices;
    }

    public Resource getLastUpdated() {
        return lastUpdated.get().asResource();
    }

    public TestResourceStore load(String resourceName) throws IOException {
        URL url = getClass().getResource(resourceName);
        JsonParser parser = new JsonParser();
        String json = com.google.common.io.Resources.toString(url, Charsets.UTF_8);
        JsonArray array = parser.parse(json).getAsJsonArray();
        for(int i=0;i!=array.size();++i) {
            Resource resource = Resources.fromJson(array.get(i).getAsJsonObject());
            resources.put(resource.getId(), resource);
            System.out.println("Loaded " + resource.getId());
        }
        return this;
    }
}
