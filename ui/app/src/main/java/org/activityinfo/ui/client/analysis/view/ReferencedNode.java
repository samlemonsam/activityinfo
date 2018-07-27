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
package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.analysis.pivot.DimensionMapping;
import org.activityinfo.model.analysis.pivot.DimensionModel;
import org.activityinfo.model.analysis.pivot.ImmutableDimensionModel;
import org.activityinfo.ui.client.icons.IconBundle;


public class ReferencedNode extends DimensionNode {

    private FormClass form;
    private FormField field;

    public ReferencedNode(FormClass form, FormField field) {
        this.form = form;
        this.field = field;
    }

    @Override
    public String getKey() {
        return "_ref:" + form.getId().asString() + "." + field.getId();
    }

    @Override
    public String getLabel() {
        return field.getLabel();
    }

    @Override
    public DimensionModel dimensionModel() {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label(form.getLabel() + " " + field.getLabel())
                .addMappings(new DimensionMapping(
                    new CompoundExpr(new SymbolNode(form.getId()), new SymbolNode(field.getId()))))
                .build();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(field.getType());
    }
}
