package org.activityinfo.ui.client.component.table.action;
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

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.style.Icons;

/**
 * @author yuriyz on 06/18/2015.
 */
public class ShowAllAction implements TableHeaderAction {

    private final InstanceTable table;
    private final String uniqueId;

    public ShowAllAction(InstanceTable table) {
        this.table = table;
        this.uniqueId = Document.get().createUniqueId();
    }

    @Override
    public void execute() {
        table.getDataLoader().loadAll();
    }

    @Override
    public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
        if (!table.getDataLoader().isAllLoaded()) {
            sb.append(TEMPLATE.enabled(uniqueId, Icons.INSTANCE.table(), I18N.CONSTANTS.showAll()));
        } else {
            sb.append(TEMPLATE.disabled(uniqueId, Icons.INSTANCE.table(), I18N.CONSTANTS.showAll()));
        }
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }
}
