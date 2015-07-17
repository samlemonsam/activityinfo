package org.activityinfo.server.endpoint.odk;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.server.command.ResourceLocatorSync;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class ItemSetBuilder {

    private final ResourceLocatorSync locator;
    private final Provider<EntityManager> entityManager;

    @Inject
    public ItemSetBuilder(ResourceLocatorSync locator, Provider<EntityManager> entityManager) {
        this.locator = locator;
        this.entityManager = entityManager;
    }

    public StreamingOutput build(ResourceId formClassId) throws IOException {

        final Set<ResourceId> rangeClassIds = findRanges(formClassId);

        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {

                ItemSetWriter writer = new ItemSetWriter(output);

                // We need at least one item set or ODK will crash
                writer.writeItem("__dummy", "dummy", "dummy");

                // Write out real item sets
                for(ResourceId formClassId : rangeClassIds) {
                    writeInstances(formClassId, writer);
                }
                writer.flush();
            }
        };
    }

    private Set<ResourceId> findRanges(ResourceId formClassId) {

        FormClass formClass = locator.getFormClass(formClassId);

        Set<ResourceId> rangeClassIds = Sets.newHashSet();
        for(FormField field : formClass.getFields()) {
            if(field.getType() instanceof ReferenceType) {
                rangeClassIds.addAll(((ReferenceType) field.getType()).getRange());
            }
        }
        return rangeClassIds;
    }

    private void writeInstances(ResourceId formClassId, ItemSetWriter writer) throws IOException {

        String listName = formClassId.asString();

        if(formClassId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN) {
            List<Tuple> instances = entityManager.get().createQuery(
                    "select g.id, g.name from Location g where g.locationType.id = :id", Tuple.class)
                    .setParameter("id", CuidAdapter.getLegacyIdFromCuid(formClassId))
                    .getResultList();

            writer.writeItem(listName, "NEW", "[NEW]");

            for(Tuple instance : instances) {
                String id = CuidAdapter.locationInstanceId(instance.get(0, Integer.class)).asString();
                String label = instance.get(1, String.class);
                writer.writeItem(listName, id, label);
            }
        }
    }

}
