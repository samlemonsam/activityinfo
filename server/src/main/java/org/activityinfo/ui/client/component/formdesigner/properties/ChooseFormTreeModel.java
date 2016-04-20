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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.*;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.adapter.FolderListAdapter;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.ui.icons.Icons;

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
        private ResourceId ownerId;
        private String label;

        public Node() {
        }

        public Node(ResourceId id, String label) {
            this(id, null, label);
        }

        public Node(ResourceId id, ResourceId ownerId, String label) {
            this.id = id;
            this.ownerId = ownerId;
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

        public ResourceId getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(ResourceId ownerId) {
            this.ownerId = ownerId;
        }

        public static List<Node> convert(List<FormInstance> result) {
            List<Node> nodes = Lists.newArrayList();
            for (FormInstance instance : result) {
                nodes.add(new Node(instance.getId(), labelFromInstance(instance)));
            }
            return nodes;
        }

        public boolean isLeaf() {
            char domain = getId().getDomain();
            return domain == CuidAdapter.ACTIVITY_DOMAIN ||
                    domain == CuidAdapter.ADMIN_LEVEL_DOMAIN ||
                    domain == CuidAdapter.PROJECT_DOMAIN ||
                    domain == CuidAdapter.PARTNER_DOMAIN;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            return !(id != null ? !id.equals(node.id) : node.id != null);

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "id=" + id +
                    ", label='" + label + '\'' +
                    '}';
        }
    }

    public interface Template extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span class='{0}'></span> {1}")
        SafeHtml cell(String icon, String label);
    }

    public static final Template TEMPLATE = GWT.create(Template.class);

    public static class Cell extends AbstractCell<Node> {

        public Cell() {
        }

        @Override
        public void render(Context context, Node value, SafeHtmlBuilder sb) {
            if (value != null) {
                sb.append(TEMPLATE.cell(icon(value), value.getLabel()));
            }
        }

        private String icon(Node node) {
            if (node.isLeaf()) {
                return Icons.INSTANCE.form();
            }

            char domain = node.getId().getDomain();

            if (domain == CuidAdapter.DATABASE_DOMAIN) {
                return Icons.INSTANCE.database();
            }

            ResourceId id = node.getId();
            if (id.equals(FolderListAdapter.MY_DATABASES) ||
                    id.equals(FolderListAdapter.SHARED_DATABASES) ||
                    id.equals(FolderListAdapter.HOME_ID) ||
                    id.equals(FolderListAdapter.GEODB_ID)) {
                return Icons.INSTANCE.home();
            }

            return Icons.INSTANCE.folder();
        }
    }

    public static class DataProvider extends AsyncDataProvider<Node> {

        private final ResourceLocator locator;
        private final Node node;

        public DataProvider(ResourceLocator locator, Node node) {
            this.locator = locator;
            this.node = node;
        }

        @Override
        protected void onRangeChanged(HasData display) {
            if (node == null) {
                loadRoots();
                return;
            }

            final Range range = display.getVisibleRange();

            locator.queryInstances(ParentCriteria.isChildOf(node.getId(), node.getOwnerId())).then(new AsyncCallback<List<FormInstance>>() {
                @Override
                public void onFailure(Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(List<FormInstance> result) {
                    List<Node> nodes = Node.convert(result);

                    addPartnerAndProjectNodeIfNeeded(node, nodes);

                    DataProvider.this.updateRowData(range.getStart(), nodes);
                    DataProvider.this.updateRowCount(nodes.size(), true);
                }
            });
        }

        private void addPartnerAndProjectNodeIfNeeded(Node parentNode, List<Node> childs) {
            if (parentNode.getId().getDomain() == CuidAdapter.DATABASE_DOMAIN) {
                childs.add(0, new Node(ClassType.PARTNER.getResourceId(), node.getId(), I18N.CONSTANTS.partners()));
                childs.add(0, new Node(ClassType.PROJECT.getResourceId(), node.getId(), I18N.CONSTANTS.projects()));
            }
        }

        private void loadRoots() {
            final List<Node> roots = Lists.newArrayList();
            roots.add(new Node(FolderListAdapter.GEODB_ID, I18N.CONSTANTS.geography()));
            roots.add(new Node(FolderListAdapter.MY_DATABASES, I18N.CONSTANTS.myDatabases()));
            roots.add(new Node(FolderListAdapter.SHARED_DATABASES, I18N.CONSTANTS.sharedDatabases()));

            updateRowData(0, roots);
            updateRowCount(roots.size(), true);
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
        return new DefaultNodeInfo<>(new DataProvider(locator, (Node) value), new Cell(), selectionModel, null);
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value instanceof Node) {
            Node node = (Node) value;
            return node.isLeaf();
        }
        return false;
    }

    public static String labelFromInstance(FormInstance instance) {
        switch (instance.getId().getDomain()) {
            case CuidAdapter.DATABASE_DOMAIN:
                return instance.getString(FolderClass.LABEL_FIELD_ID);
            case CuidAdapter.COUNTRY_DOMAIN:
                return instance.getString(ApplicationProperties.COUNTRY_NAME_FIELD);
            case CuidAdapter.ADMIN_LEVEL_DOMAIN:
                return instance.getString(ResourceId.valueOf(FormClass.LABEL_FIELD_ID));
            case CuidAdapter.ACTIVITY_DOMAIN:
                return instance.getString(ResourceId.valueOf(FormClass.LABEL_FIELD_ID));
            case CuidAdapter.ACTIVITY_CATEGORY_DOMAIN:
                return instance.getString(FolderClass.LABEL_FIELD_ID);
        }

        String label = FormInstanceLabeler.getLabel(instance);
        return !Strings.isNullOrEmpty(label) ? label : I18N.CONSTANTS.unknown();
    }
}
