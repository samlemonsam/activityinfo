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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.common.base.Strings;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.EntityDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UniqueNameValidator implements Validator {

    private Set<String> usedNames;

    public UniqueNameValidator(Set<String> usedNames) {
        this.usedNames = usedNames;
    }

    public UniqueNameValidator(List<? extends ModelData> models, String currentName) {
        this.usedNames = new HashSet<>();
        for (ModelData model : models) {
            String name = model.get(EntityDTO.NAME_PROPERTY);
            if(!Strings.isNullOrEmpty(name) && !Objects.equals(name, currentName)) {
                usedNames.add(name);
            }
        }
    }
    public UniqueNameValidator(List<? extends ModelData> models) {
        this(models, "");
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