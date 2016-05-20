package org.activityinfo.ui.client.component.table.filter;
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

import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTable;

/**
 * @author yuriyz on 4/3/14.
 */
public class FilterContentFactory {

    private FilterContentFactory() {
    }

    public static FilterContent create(FieldColumn column, InstanceTable table, FilterPanel popup) {
        FieldType type = column.getNode().getField().getType();
        if (type == LocalDateType.INSTANCE) {
            return new FilterContentDate(column);
        } else if (type instanceof AttachmentType) {
            return blankPanel();
        }
        return new FilterContentExistingItems(column, table, popup);
    }

    private static FilterContent blankPanel() {
        return new FilterContent() {
            @Override
            public Widget asWidget() {
                return new Label(I18N.CONSTANTS.filterIsNotSupported());
            }

            @Override
            public void clear() {
            }

            @Override
            public boolean isValid() {
                return false;
            }

            @Override
            public void setChangeHandler(ValueChangeHandler handler) {
            }

            @Override
            public ExprNode getFilter() {
                return null;
            }
        };
    }
}
