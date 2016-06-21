package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Created by yuriyz on 6/21/2016.
 */
public class CompositeValidator implements Validator {

    private Set<Validator> validators = Sets.newHashSet();

    public CompositeValidator(Collection<Validator> validators) {
        this.validators.addAll(validators);
    }

    public CompositeValidator(Validator... validators) {
        if (validators != null) {
            this.validators.addAll(Arrays.asList(validators));
        }
    }

    public void addValidator(Validator validator) {
        validators.add(validator);
    }

    @Override
    public String validate(Field<?> field, String value) {
        for (Validator validator : validators) {
            String validate = validator.validate(field, value);
            if (validate != null) {
                return validate;
            }
        }
        return null;
    }
}
