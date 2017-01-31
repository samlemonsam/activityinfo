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
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author yuriyz on 4/8/14.
 */
public interface TableHeaderAction {

    String ACTION_ATTRIBUTE = "header_action";

    interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<button class='btn btn-default btn-xs' type='button' tabindex='-1' header_action='{0}'><span class='{1}'></span> {2}</button>")
        SafeHtml enabled(String uniqueId, String icon, String text);

        @SafeHtmlTemplates.Template("<button class='btn btn-default btn-xs' type='button' tabindex='-1' disabled='disabled' header_action='{0}'><span class='{1}'></span> {2}</button>")
        SafeHtml disabled(String uniqueId, String icon, String text);

        @SafeHtmlTemplates.Template("<div class='pull-right'>" +
                "<button class='btn btn-default btn-xs' type='button' tabindex='-1' header_action='{0}'><span class='{1}'></span> {2}</button>" +
                "</div>")
        SafeHtml rightAlignedButton(String uniqueId, String icon, String text);
    }

    Template TEMPLATE = GWT.create(Template.class);

    void execute();

    void render(Cell.Context context, String value, SafeHtmlBuilder sb);

    String getUniqueId();
}
