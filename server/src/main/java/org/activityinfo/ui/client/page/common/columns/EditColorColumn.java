package org.activityinfo.ui.client.page.common.columns;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.report.editor.map.layerOptions.PiechartLayerOptions;
import org.activityinfo.ui.client.component.report.editor.map.layerOptions.PiechartLayerOptions.NamedSlice;
import org.activityinfo.ui.client.widget.legacy.ColorField;

public class EditColorColumn extends ColumnConfig {

    interface Templates extends SafeHtmlTemplates {
        @Template("<span style=\"background:#{0}\">{0}</span>")
        SafeHtml colorCell(String color);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    /**
     * Assumes the model has a property String getColor()
     */
    public EditColorColumn() {
        super("color", I18N.CONSTANTS.color(), 50);
        setToolTip(I18N.CONSTANTS.color());
        final ColorField colorField = new ColorField();

        GridCellRenderer<NamedSlice> colorRenderer = new GridCellRenderer<PiechartLayerOptions.NamedSlice>() {
            @Override
            public SafeHtml render(NamedSlice model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<NamedSlice> store,
                                   Grid<NamedSlice> grid) {
                return TEMPLATES.colorCell(model.getColor());
            }
        };

        setRenderer(colorRenderer);
        setEditor(new CellEditor(colorField));
    }

}
