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
package org.activityinfo.ui.client.component.form;

import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.component.form.subform.RepeatingSubFormPanel;

import java.util.Map;

/**
 * @author yuriyz on 01/25/2016.
 */
public class SubFormsHandler {

    private final Map<FormClass, RepeatingSubFormPanel> subForms = Maps.newHashMap();

    public SubFormsHandler() {
    }

    public Map<FormClass, RepeatingSubFormPanel> getSubForms() {
        return subForms;
    }

    public boolean validate() {
        boolean valid = true;
        for (RepeatingSubFormPanel manipulator : subForms.values()) {
            for (SimpleFormPanel subFormPanel : manipulator.getForms().values()) {
                if (!subFormPanel.validate()) {
                    valid = false;
                }
            }
        }
        return valid;
    }
}
