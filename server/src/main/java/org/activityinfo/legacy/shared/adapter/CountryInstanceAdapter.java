package org.activityinfo.legacy.shared.adapter;


import com.google.common.base.Function;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.CountryDTO;

import javax.annotation.Nullable;

public class CountryInstanceAdapter implements Function<CountryDTO, FormInstance> {


    @Nullable @Override
    public FormInstance apply(CountryDTO input) {
        ResourceId classId = CuidAdapter.cuid(CuidAdapter.COUNTRY_DOMAIN, input.getId());
        FormInstance instance = new FormInstance(classId, ApplicationProperties.COUNTRY_CLASS);
        instance.set(ApplicationProperties.COUNTRY_NAME_FIELD, input.getName());
        return instance;
    }
}
