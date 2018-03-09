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
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.EntityDTO;

import java.util.Collection;
import java.util.Set;

/**
 * Created by yuriyz on 6/21/2016.
 */
public class UniqueNameValidator implements Validator {

    public static class UniqueNamesFunction implements Function<Void, Set<String>> {

        private final Collection<? extends EntityDTO> entities;

        public UniqueNamesFunction(Collection<? extends EntityDTO> entities) {
            this.entities = entities;
        }

        @Override
        public Set<String> apply(Void input) {
            Set<String> names = Sets.newHashSet();
            for (EntityDTO entity : entities) {
                names.add(entity.getName());
            }
            return names;
        }
    }

    private Set<String> usedNames;

    public UniqueNameValidator(Set<String> usedNames) {
        this.usedNames = usedNames;
    }

    public UniqueNameValidator(Collection<? extends EntityDTO> entities) {
        this(new UniqueNamesFunction(entities));
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