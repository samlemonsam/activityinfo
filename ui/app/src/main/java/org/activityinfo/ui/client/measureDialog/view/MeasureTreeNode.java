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
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.table.ColumnDialog;

import java.util.Collection;
import java.util.Optional;


public abstract class MeasureTreeNode {

    public abstract String getId();

    public abstract String getLabel();

    public abstract ImageResource getIcon();

    public abstract MeasureModel newMeasure();

    public Optional<TableColumn> newTableColumn() {
        return Optional.empty();
    }
}
