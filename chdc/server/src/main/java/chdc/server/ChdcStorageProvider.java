package chdc.server;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormCatalog;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class ChdcStorageProvider implements FormStorageProvider, FormCatalog {

    private static final Logger LOGGER = Logger.getLogger(ChdcStorageProvider.class.getName());

    private Map<ResourceId, FormClass> schemas = new HashMap<>();

    public ChdcStorageProvider() {
        loadSchema("schema/act.json");
        loadSchema("schema/actor.json");
        loadSchema("schema/actor_category.json");
        loadSchema("schema/country.json");
        loadSchema("schema/incident.json");
        loadSchema("schema/life_impact.json");
        loadSchema("schema/location.json");
        loadSchema("schema/means.json");
//        loadSchema("schema/property_impact.json");

    }

    /**
     * Loads the FormSchema from a JSON-encoded resource.
     */
    private void loadSchema(String resourceName) {
        try {
            URL url = Resources.getResource(resourceName);
            String formSchemaJson = Resources.toString(url, Charsets.UTF_8);
            FormClass formClass = FormClass.fromJson(formSchemaJson);

            schemas.put(formClass.getId(), formClass);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load schema: " + resourceName, e);
        }
    }


    @Override
    public FormClass getFormClass(ResourceId formId) {
        return schemas.get(formId);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> schemas = new HashMap<>();
        for (ResourceId formId : formIds) {
            schemas.put(formId, getFormClass(formId));
        }
        return schemas;
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
        List<CatalogEntry> entries = new ArrayList<>();
        for (FormClass formClass : schemas.values()) {
            if(!formClass.isSubForm()) {
                CatalogEntry entry = new CatalogEntry(
                        formClass.getId().asString(),
                        formClass.getLabel(), CatalogEntryType.FORM);
                entries.add(entry);
            }
        }
        return entries;
    }

    @Override
    public List<CatalogEntry> getChildren(String parentId, int userId) {
        return Collections.emptyList();
    }
}
