package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.activityinfo.model.legacy.CuidAdapter.*;

/**
 * Extracts a list of databases as a list of folders
 */
public class FolderListAdapter implements Function<SchemaDTO, List<FormInstance>> {

    public static final ResourceId HOME_ID = ResourceId.valueOf("home");
    public static final ResourceId GEODB_ID = ResourceId.valueOf("_geodb");

    private final Criteria criteria;

    public FolderListAdapter(Criteria criteria) {
        this.criteria = criteria;
    }

    @Nullable @Override
    public List<FormInstance> apply(SchemaDTO schemaDTO) {
        List<FormInstance> instances = Lists.newArrayList();

        FormInstance root = new FormInstance(HOME_ID, FolderClass.CLASS_ID);
        root.set(FolderClass.LABEL_FIELD_ID, I18N.CONSTANTS.home());
        if (criteria.apply(root)) {
            instances.add(root);
        }

        FormInstance geodb = new FormInstance(GEODB_ID, FolderClass.CLASS_ID);
        root.set(FolderClass.LABEL_FIELD_ID, "Geographic Reference Database");
        if (criteria.apply(geodb)) {
            instances.add(geodb);
        }

        for (UserDatabaseDTO db : schemaDTO.getDatabases()) {
            FormInstance dbFolder = newFolder(db);
            if (criteria.apply(dbFolder)) {
                instances.add(dbFolder);
            }

            Set<String> categories = new HashSet<>();

            for (ActivityDTO activity : db.getActivities()) {

                FormInstance activityClass = new FormInstance(activityFormClass(activity.getId()), FormClass.CLASS_ID);

                if (!Strings.isNullOrEmpty(activity.getCategory())) {
                    categories.add(activity.getCategory());
                    activityClass.setOwnerId(activityCategoryFolderId(db.getId(), activity.getCategory()));
                } else {
                    activityClass.setOwnerId(databaseId(db.getId()));
                }

                activityClass.set(FormClass.LABEL_FIELD_ID, activity.getName());

                if (criteria.apply(activityClass)) {
                    instances.add(activityClass);
                }
            }

            for (String category : categories) {
                FormInstance categoryFolder = new FormInstance(activityCategoryFolderId(db.getId(), category),
                        FolderClass.CLASS_ID);
                categoryFolder.setOwnerId(dbFolder.getId());
                categoryFolder.set(FolderClass.LABEL_FIELD_ID, category);

                if (criteria.apply(categoryFolder)) {
                    instances.add(categoryFolder);
                }
            }
        }

        // Add LocationTypes which have been assigned to a database
        for (CountryDTO country : schemaDTO.getCountries()) {
            for (LocationTypeDTO locationType : country.getLocationTypes()) {
                if (!locationType.isAdminLevel() && locationType.getDatabaseId() != null) {
                    FormInstance instance = new FormInstance(CuidAdapter.locationFormClass(locationType.getId()),
                            FormClass.CLASS_ID);
                    instance.set(FormClass.LABEL_FIELD_ID, locationType.getName());
                    instance.setOwnerId(CuidAdapter.cuid(DATABASE_DOMAIN, locationType.getDatabaseId()));

                    if (criteria.apply(instance)) {
                        instances.add(instance);
                    }
                }
            }
        }

        return instances;
    }

    private FormInstance newFolder(UserDatabaseDTO db) {
        FormInstance folder = new FormInstance(cuid(DATABASE_DOMAIN, db.getId()), FolderClass.CLASS_ID);
        folder.setOwnerId(HOME_ID);
        folder.set(FolderClass.LABEL_FIELD_ID, db.getName());
        folder.set(FolderClass.DESCRIPTION_FIELD_ID, db.getFullName());
        return folder;
    }

}
