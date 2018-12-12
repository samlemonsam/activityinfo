package org.activityinfo.model.database;

import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for catalog lists in UserDatabaseMeta#findCatalogEntries
 */
public class UserDatabaseMetaCatalogTest {

    private DatabaseModelTestResources RESOURCES = new DatabaseModelTestResources();

    @Test
    public void catalogTest() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        UserDatabaseMeta db = UserDatabaseMeta.buildOwnedUserDatabaseMeta(meta);

        rootEntries(db);
        folderEntries(db);
        subFormEntries(db);
    }

    private void rootEntries(UserDatabaseMeta db) {
        // Each root entry should have:
        // - an id equal to the resource id
        // - a label equal to the resource label
        // - an isLeaf boolean of TRUE, EXCEPT for the folder resource which is FALSE
        // - a type of FORM, EXCEPT for the folder resource which has type FOLDER
        Map<ResourceId,Resource> rootResources = RESOURCES.rootResources().stream().collect(Collectors.toMap(Resource::getId,r -> r));
        List<CatalogEntry> rootEntries = db.findCatalogEntries(RESOURCES.DB_ID);

        assert rootEntries.size() == rootResources.size();
        for (CatalogEntry rootEntry : rootEntries) {
            ResourceId rootEntryId = ResourceId.valueOf(rootEntry.getId());
            assert rootResources.containsKey(rootEntryId);
            assert rootResources.get(rootEntryId).getLabel().equals(rootEntry.getLabel());
            if (!rootEntryId.equals(RESOURCES.FOLDER_ID)) {
                assert rootEntry.isLeaf();
                assert rootEntry.getType().equals(CatalogEntryType.FORM);
            } else {
                assert !rootEntry.isLeaf();
                assert rootEntry.getType().equals(CatalogEntryType.FOLDER);
            }
        }
    }

    private void folderEntries(UserDatabaseMeta db) {
        // Each folder entry should have:
        // - an id equal to the resource id
        // - a label equal to the resource label
        // - an isLeaf boolean of FALSE, as the FORM should have a nested subform
        // - a type of FORM
        Map<ResourceId,Resource> folderResources = RESOURCES.folderResources().stream().collect(Collectors.toMap(Resource::getId,r -> r));
        List<CatalogEntry> rootEntries = db.findCatalogEntries(RESOURCES.FOLDER_ID);

        assert rootEntries.size() == folderResources.size();
        for (CatalogEntry rootEntry : rootEntries) {
            ResourceId rootEntryId = ResourceId.valueOf(rootEntry.getId());
            assert folderResources.containsKey(rootEntryId);
            assert folderResources.get(rootEntryId).getLabel().equals(rootEntry.getLabel());
            assert !rootEntry.isLeaf();
            assert rootEntry.getType().equals(CatalogEntryType.FORM);
        }
    }

    private void subFormEntries(UserDatabaseMeta db) {
        // Single sub form entry should have:
        // - an id equal to the resource id
        // - a label equal to the resource label
        // - an isLeaf boolean of TRUE
        // - a type of FORM
        List<CatalogEntry> rootEntries = db.findCatalogEntries(RESOURCES.FOLDER_FORM_ID);
        assert rootEntries.size() == 1;
        CatalogEntry subFormEntry = rootEntries.get(0);
        ResourceId subFormEntryId = ResourceId.valueOf(subFormEntry.getId());
        assert subFormEntryId.equals(RESOURCES.SUB_FORM_ID);
        assert subFormEntry.getLabel().equals(RESOURCES.subFormResource().getLabel());
        assert subFormEntry.isLeaf();
        assert subFormEntry.getType().equals(CatalogEntryType.FORM);
    }

}