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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

class DesignIconProvider implements ModelIconProvider<ModelData> {
    @Override
    public AbstractImagePrototype getIcon(ModelData model) {
        if (model instanceof IsActivityDTO) {
            IsActivityDTO activity = (IsActivityDTO) model;
            if (activity.getClassicView()) {
                return IconImageBundle.ICONS.activity();
            } else {
                return IconImageBundle.ICONS.form();
            }
        } else if (model instanceof FieldGroup || model instanceof FolderDTO) {
            return GXT.IMAGES.tree_folder_closed();

        } else if (model instanceof AttributeGroupDTO) {
            return IconImageBundle.ICONS.attributeGroup();

        } else if (model instanceof AttributeDTO) {
            return IconImageBundle.ICONS.attribute();

        } else if (model instanceof IndicatorDTO) {
            return IconImageBundle.ICONS.indicator();

        } else if (model instanceof LocationTypeDTO) {
            return IconImageBundle.ICONS.marker();
        } else {
            return null;
        }
    }
}
