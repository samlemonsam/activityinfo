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
package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

public class ReferenceNode extends MeasureTreeNode {

    private FormTree.Node node;

    public ReferenceNode(FormTree.Node node) {
        this.node = node;
    }

    @Override
    public String getId() {
        return node.getPath().toString();
    }

    @Override
    public String getLabel() {
        return node.getField().getLabel();
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.iconForField(node.getType());
    }

    @Override
    public MeasureModel newMeasure() {
        throw new UnsupportedOperationException();
    }
}
