package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;

import java.util.Set;

/**
 * Created by yuriyz on 6/21/2016.
 */
public class UniqueNameValidator implements Validator {

    private Set<String> usedNames;

    public UniqueNameValidator(Set<String> usedNames) {
        this.usedNames = usedNames;
    }

    public UniqueNameValidator(Function<Void, Set<String>> uniqueNamesFunction) {
        this.usedNames = uniqueNamesFunction.apply(null);
    }

    @Override
    public String validate(Field<?> field, String value) {
        if (value == null || Strings.isNullOrEmpty(value.trim())) {
            return I18N.CONSTANTS.blankValueIsNotAllowed();
        }
        if (usedNames.contains(value.trim())) {
            return I18N.CONSTANTS.duplicateName();
        }
        return null;
    }
}