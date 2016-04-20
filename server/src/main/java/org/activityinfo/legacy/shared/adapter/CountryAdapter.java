package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import org.activityinfo.legacy.shared.model.CountryDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;

/**
 * Created by yuriyz on 4/19/2016.
 */
public class CountryAdapter implements Function<SchemaDTO, FormClass> {

    private int countryId;

    public CountryAdapter(int countryId) {
        this.countryId = countryId;
    }

    @Override
    public FormClass apply(SchemaDTO schema) {
        CountryDTO country = schema.getCountryById(countryId);

        FormClass formClass = new FormClass(CuidAdapter.countryId(countryId));
        formClass.setLabel(country.getName());

        return formClass;
    }
}
