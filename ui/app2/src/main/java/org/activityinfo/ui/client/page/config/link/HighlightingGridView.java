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
package org.activityinfo.ui.client.page.config.link;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;

import java.util.List;
import java.util.Set;

class HighlightingGridView extends GridView {

    public static final EventType ROW_MOUSE_OVER = new EventType();

    private List<Element> highlighted = Lists.newArrayList();

    @Override
    protected void onRowOut(Element row) {
        fly(row).removeStyleName(IndicatorLinkResources.INSTANCE.style().highlight());
    }

    @Override
    protected void onRowOver(Element row) {
        int index = findRowIndex(row);
        if (index != -1) {
            ModelData model = grid.getStore().getAt(index);
            if (isHighlightable(model)) {

                fly(row).addStyleName(IndicatorLinkResources.INSTANCE.style().highlight());

                GridEvent event = new GridEvent(grid);
                event.setModel(model);

                grid.fireEvent(HighlightingGridView.ROW_MOUSE_OVER, event);
            }
        }
        overRow = row;
    }

    public void highlight(Set<Integer> ids) {
        clearHighlight();
        if (!ids.isEmpty()) {
            for (int row = 0; row != grid.getStore().getCount(); ++row) {
                Element element = grid.getView().getRow(row);
                El el = El.fly(element);
                if (ids.contains(grid.getStore().getAt(row).get("id"))) {
                    el.addStyleName(IndicatorLinkResources.INSTANCE.style().highlight());
                    highlighted.add(element);
                }
            }
        }
    }

    public void clearHighlight() {
        for (Element element : highlighted) {
            El.fly(element).removeStyleName(IndicatorLinkResources.INSTANCE.style().highlight());
        }
        highlighted.clear();
    }

    protected boolean isHighlightable(ModelData model) {
        return true;
    }

    public void refreshAllRows() {
        for (int i = 0; i != getRows().getLength(); ++i) {
            refreshRow(i);
        }
    }
}