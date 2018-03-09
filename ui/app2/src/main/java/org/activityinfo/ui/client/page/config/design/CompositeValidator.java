/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
