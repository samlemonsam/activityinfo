package org.activityinfo.ui.client.component.formdesigner.properties;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.*;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * @author yuriyz on 04/06/2016.
 */
public class ChooseFormTreeModel implements TreeViewModel {

    public static class Node {

        public static final ProvidesKey<Node> KEY_PROVIDER = new ProvidesKey<Node>() {
            @Override
            public Object getKey(Node node) {
                return node == null ? null : node.getId();
            }
        };

        private ResourceId id;
        private String label;

        public Node() {
        }

        public Node(ResourceId id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public ResourceId getId() {
            return id;
        }

        public void setId(ResourceId id) {
            this.id = id;
        }
    }

    public static class Cell extends AbstractCell<Node> {

        public Cell() {
        }

        @Override
        public void render(Context context, Node value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(SafeHtmlUtils.fromString(value.getLabel()));
            }
        }
    }

    public static class DataProvider extends AsyncDataProvider<Node> {

        private final ResourceLocator locator;

        public DataProvider(ResourceLocator locator) {
            this.locator = locator;
        }

        @Override
        protected void onRangeChanged(HasData display) {
            Range range = display.getVisibleRange();

            locator.queryInstances(new ClassCriteria(FolderClass.CLASS_ID)).then(new AsyncCallback<List<FormInstance>>() {
                @Override
                public void onFailure(Throwable caught) {

                }

                @Override
                public void onSuccess(List<FormInstance> result) {

                }
            });
        }
    }

    private final ResourceLocator locator;
    private final MultiSelectionModel<Node> selectionModel;

    public ChooseFormTreeModel(ResourceLocator locator, MultiSelectionModel<Node> selectionModel) {
        this.locator = locator;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            return new DefaultNodeInfo<>(new DataProvider(locator), new Cell(), selectionModel, null);

        }
        throw new RuntimeException("Failed to identify child of the value: " + value);
    }

    @Override
    public boolean isLeaf(Object value) {
        return false;
    }
}
