package chdc.server;

import com.google.common.base.Optional;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorage;

import java.util.*;

public class ChdcCatalog implements FormCatalog {

    private Map<ResourceId, FormClass> schemas = new HashMap<>();

    public ChdcCatalog() {
        FormClass countryForm = new FormClass(ResourceId.valueOf("country"));
        countryForm.setLabel("Country");
        countryForm.addField(ResourceId.valueOf("name"))
                .setLabel("Name")
                .setType(TextType.SIMPLE)
                .setRequired(true)
                .setKey(true);

        schemas.put(countryForm.getId(), countryForm);
    }


    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {
        if(schemas.containsKey(formId)) {
            return Optional.<FormStorage>of(new MySqlFormStorage(schemas.get(formId)));
        }
        return Optional.absent();
    }

    @Override
    public List<CatalogEntry> getRootEntries() {
        return Collections.emptyList();
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return Collections.emptyList();
    }
}
