package org.activityinfo.ui.client.component.chooseForm;
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

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.*;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.ui.icons.Icons;

import java.util.List;

/**
 * @author yuriyz on 04/06/2016.
 */
class ChooseFormTreeModel implements TreeViewModel {


    static final ProvidesKey<CatalogEntry> KEY_PROVIDER = new ProvidesKey<CatalogEntry>() {
        @Override
        public Object getKey(CatalogEntry node) {
            return node == null ? null : node.getId();
        }
    };

    public interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span class='{0}'></span> {1}")
        SafeHtml cell(String icon, String label);
    }

    public static final Template TEMPLATE = GWT.create(Template.class);

    public static class Cell extends AbstractCell<CatalogEntry> {

        public Cell() {
        }

        @Override
        public void render(Context context, CatalogEntry value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(TEMPLATE.cell(icon(value), value.getLabel()));
            }
        }

        private String icon(CatalogEntry node) {
            if (node.getType() == CatalogEntryType.FORM) {
                return Icons.INSTANCE.form();
            } else {
                return Icons.INSTANCE.folder();
            }
        }
    }

    private static class DataProvider extends AsyncDataProvider<CatalogEntry> {

        private final ResourceLocator locator;
        private final String parentId;

        DataProvider(ResourceLocator locator, CatalogEntry parent) {
            this.locator = locator;
            if(parent == null) {
                this.parentId = null;
            } else {
                this.parentId = parent.getId();
            }
        }

        @Override
        protected void onRangeChanged(HasData display) {
            

            final Range range = display.getVisibleRange();
            
            locator.getCatalogEntries(parentId).then(new AsyncCallback<List<CatalogEntry>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(List<CatalogEntry> entries) {
                    DataProvider.this.updateRowData(range.getStart(), entries);
                    DataProvider.this.updateRowCount(entries.size(), true);
                }
            });
        }
    }

    private final ResourceLocator locator;
    private final MultiSelectionModel<CatalogEntry> selectionModel;

    public ChooseFormTreeModel(ResourceLocator locator, MultiSelectionModel<CatalogEntry> selectionModel) {
        this.locator = locator;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        return new DefaultNodeInfo<>(new DataProvider(locator, (CatalogEntry)value), new Cell(), selectionModel, null);
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value instanceof CatalogEntry) {
            CatalogEntry entry = (CatalogEntry) value;
            if(entry.getType() == CatalogEntryType.FOLDER) {
                return false;
            }
        }
        return true;
    }
}
