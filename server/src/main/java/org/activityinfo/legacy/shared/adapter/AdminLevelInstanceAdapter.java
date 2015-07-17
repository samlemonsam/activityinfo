package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;

import javax.annotation.Nullable;

public class AdminLevelInstanceAdapter implements Function<AdminLevelDTO, FormInstance> {

    @Nullable @Override
    public FormInstance apply(AdminLevelDTO input) {
        FormInstance instance = new FormInstance(CuidAdapter.adminLevelFormClass(input.getId()), FormClass.CLASS_ID);

        instance.setOwnerId(CuidAdapter.cuid(CuidAdapter.COUNTRY_DOMAIN, input.getCountryId()));
        instance.set(FormClass.LABEL_FIELD_ID, input.getName());

        return instance;
    }
}
