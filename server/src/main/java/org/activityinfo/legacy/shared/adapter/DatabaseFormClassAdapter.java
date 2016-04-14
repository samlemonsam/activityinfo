package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;

/**
 * Created by yuriyz on 4/14/2016.
 */
public class DatabaseFormClassAdapter implements Function<SchemaDTO, FormClass> {

    private final int databaseId;

    public DatabaseFormClassAdapter(int databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public FormClass apply(SchemaDTO input) {
        UserDatabaseDTO database = input.getDatabaseById(databaseId);

        FormClass formClass = new FormClass(CuidAdapter.databaseId(databaseId));
        formClass.setLabel(database.getName());
        return formClass;
    }
}
