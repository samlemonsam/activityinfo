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
package org.activityinfo.geoadmin.merge2.view.profile;

import com.google.common.base.Strings;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.Extents;
import org.activityinfo.model.type.geo.GeoAreaType;

/**
 * Describes the contents of a field in the source or target form.
 */
public class FieldProfile {

    private FormTree.Node node;
    private ColumnView columnView;

    private ColumnView xmin;
    private ColumnView xmax;
    private ColumnView ymin;
    private ColumnView ymax;

    public FieldProfile(FormTree.Node node, ColumnView columnView) {
        this.node = node;
        this.columnView = columnView;
    }

    public FieldProfile(FormTree.Node node, ColumnView xmin, ColumnView xmax, ColumnView ymin, ColumnView ymax) {
        this.node = node;
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
    }

    public int getNumRows() {
        if(columnView != null) {
            return columnView.numRows();
        } else if(xmax != null) {
            return xmax.numRows();
        } else {
            return 0;
        }
    }

    public boolean isText() {
        if(columnView == null) {
            return false;
        }
        return columnView.getType() == ColumnType.STRING;
    }
    
    public boolean isGeoArea() {
        if(xmin == null) {
            return false;
        }
        return node.getType() instanceof GeoAreaType;
    }


    public ResourceId getId() {
        return node.getFieldId();
    }

    public String getLabel() {
        if(node.isRoot()) {
            return node.getField().getLabel();
        } else {
            return node.getDefiningFormClass().getLabel() + "." + node.getField().getLabel();
        }
    }

    public ColumnView getView() {
        return columnView;
    }

    public Extents getExtents(int index) {
        return new Extents(ymin.getDouble(index),
                           ymax.getDouble(index),
                           xmin.getDouble(index),
                           xmax.getDouble(index));
    }

    public String getCode() {
        return node.getField().getCode();
    }

    public FieldPath getPath() {
        return node.getPath();
    }

    public FormTree.Node getNode() {
        return node;
    }
    
    public FormField getFormField() {
        return node.getField();
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public boolean hasIdenticalContents(ColumnView view) {
        // Can only find text fields to be identical
        if(!this.isText() || view.getType() != ColumnType.STRING) {
            return false;
        }
    
        for (int i = 0; i < view.numRows(); i++) {
            String x = Strings.nullToEmpty(this.columnView.getString(i));
            String y = Strings.nullToEmpty(view.getString(i));
            if(!x.equalsIgnoreCase(y)) {
                return false;
            }
        }
        return true;
    }

    public String getString(int sourceRow) {
        if(xmax != null) {
            return "(Geometry)";
        } else {
            return columnView.getString(sourceRow);
        }
    }
}
