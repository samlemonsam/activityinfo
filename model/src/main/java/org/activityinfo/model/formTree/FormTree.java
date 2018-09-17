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
package org.activityinfo.model.formTree;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.*;

/**
 * Contains a tree of fields based on references to other {@code FormClasses}
 */
public class FormTree implements FormClassProvider, FormMetadataProvider {

    private ResourceId rootFormId;


    public enum State {
        VALID,
        DELETED,
        FORBIDDEN
    }

    public class Node {

        private Node parent;
        private FormField field;

        private FieldPath path;
        private FormMetadata form;
        private List<Node> children = Lists.newArrayList();

        private int depth;

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isReference() {
            return field.getType() instanceof ReferenceType ||
                    field.getType() instanceof SubFormReferenceType;
        }

        public boolean isParentReference() {
            if(!form.getSchema().isSubForm()) {
                return false;
            }
            if(!(field.getType() instanceof ReferenceType)) {
                return false;
            }
            ReferenceType type = (ReferenceType) getType();
            if(type.getRange().size() != 1) {
                return false;
            }
            ResourceId rangeFormId = Iterables.getOnlyElement(type.getRange());
            ResourceId parentFormId = form.getSchema().getParentFormId().get();

            return rangeFormId.equals(parentFormId);
        }

        public boolean isEnum() {
            return field.getType() instanceof EnumType;
        }

        public Node addChild(FormMetadata declaringClass, FormField field) {
            FormTree.Node childNode = new FormTree.Node();
            childNode.parent = this;
            childNode.field = field;
            childNode.path = new FieldPath(this.path, field.getId());
            childNode.form = declaringClass;
            children.add(childNode);
            nodeMap.put(childNode.path, childNode);

            if (childNode.parent != null) {
                childNode.depth = childNode.parent.depth + 1;
            }
            return childNode;
        }


        /**
         *
         * @return the fields that are defined on the classes in this Field's range.
         */
        public List<Node> getChildren() {
            return children;
        }

        /**
         * @return the fields that are defined one of the classes in this field's range
         */
        public Iterable<Node> getChildren(ResourceId formClassId) {
            List<Node> matching = Lists.newArrayList();
            for (Node child : children) {
                if(child.getDefiningFormClass().getId().equals(formClassId)) {
                    matching.add(child);
                }
            }
            return matching;
        }

        public FieldPath getPath() {
            return path;
        }

        public FormField getField() {
            return field;
        }

        /**
         *
         * @return the form class which has defined this form
         */
        public FormClass getDefiningFormClass() {
            return form.getSchema();
        }

        public FormMetadata getForm() {
            return form;
        }

        public ResourceId getFieldId() {
            return field.getId();
        }

        /**
         *
         * @return for Reference fields, the range of this field
         */
        public Collection<ResourceId> getRange() {
            if(field.getType() instanceof ReferenceType) {
                return ((ReferenceType) field.getType()).getRange();

            } else if(field.getType() instanceof SubFormReferenceType) {
                SubFormReferenceType subFormType = (SubFormReferenceType) field.getType();
                ResourceId subFormClassId = subFormType.getClassId();
                return Collections.singleton(subFormClassId);

            } else {
                return Collections.emptySet();
            }
        }

        public FieldType getType() {
            return field.getType();
        }

        public FieldTypeClass getTypeClass() {
            return field.getType().getTypeClass();
        }

        public Node getParent() {
            return parent;
        }

        /**
         *
         * @return a readable path for this node for debugging
         */
        public String debugPath() {
            StringBuilder path = new StringBuilder();
            path.append(toString(this.getField().getLabel(), this.getDefiningFormClass()));
            Node parent = this.parent;
            while(parent != null) {
                path.insert(0, toString(parent.getField().getLabel(), parent.getDefiningFormClass()) + ".");
                parent = parent.parent;
            }
            return path.toString();
        }

        @Override
        public String toString() {
            return toString(field.getLabel(), this.getDefiningFormClass()) + ":" + field.getType();
        }

        public String fieldLabel() {
            return toLabel(field.getLabel());
        }

        public String formFieldLabel() {
            return getDefiningFormClass() != null
                    ? toLabel(getDefiningFormClass().getLabel()) + "." + toLabel(field.getLabel())
                    : toLabel(field.getLabel());
        }

        private String toLabel(String label) {
            return label.toLowerCase().trim().replace(" ","_");
        }

        private String toString(String label, FormClass definingFormClass) {
            String field = "[";
            if(definingFormClass != null && definingFormClass.getLabel() != null)  {
                field += definingFormClass.getLabel() + ":";
            }
            field += label;
            field += "]";

            return field;
        }

        public Node findDescendant(FieldPath relativePath) {
            FieldPath path = new FieldPath(getPath(), relativePath);
            return findDescendantByAbsolutePath(path);
        }

        private Node findDescendantByAbsolutePath(FieldPath path) {
            if(this.path.equals(path)) {
                return this;
            } else {
                for(Node child : children) {
                    Node descendant = child.findDescendantByAbsolutePath(path);
                    if(descendant != null) {
                        return descendant;
                    }
                }
                return null;
            }
        }

        public boolean isLeaf() {
            return children.isEmpty();
        }

        public int getDepth() {
            return depth;
        }


        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public FormClass getRootFormClass() {
            if(isRoot()) {
                return getDefiningFormClass();
            } else {
                return getParent().getRootFormClass();
            }
        }

        public List<Node> getSelfAndAncestors() {
            LinkedList<Node> list = Lists.newLinkedList();
            Node node = this;
            while(!node.isRoot()) {
                list.addFirst(node);
                node = node.getParent();
            }
            list.addFirst(node);
            return list;
        }

        public boolean isLinked() {
            return parent != null && (parent.isLinked() || parent.getType() instanceof ReferenceType);
        }
        public boolean isCalculated() {
            return getType() instanceof CalculatedFieldType;
        }

        public Iterator<Node> selfAndAncestors() {
            return new UnmodifiableIterator<Node>() {

                private Node next = Node.this;

                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Node next() {
                    Node toReturn = next;
                    next = next.getParent();
                    return toReturn;
                }
            };
        }

        public boolean isSubForm() {
            return getType() instanceof SubFormReferenceType;
        }

        public boolean isSubFormVisible() {
            assert isSubForm();
            SubFormReferenceType type = (SubFormReferenceType) field.getType();
            FormMetadata subForm = getFormMetadata(type.getClassId());
            return subForm.isVisible();
        }
    }

    public enum SearchOrder {
        DEPTH_FIRST,
        BREADTH_FIRST
    }

    private State rootState = State.VALID;
    private List<Node> rootFields = Lists.newArrayList();
    private Map<FieldPath, Node> nodeMap = Maps.newHashMap();
    private Map<ResourceId, FormMetadata> formMap = new HashMap<>();

    public FormTree(ResourceId rootFormId) {
        this.rootFormId = rootFormId;
    }

    public ResourceId getRootFormId() {
        return rootFormId;
    }

    public State getRootState() {
        return rootState;
    }

    public void setRootState(State rootState) {
        this.rootState = rootState;
    }

    public Node addRootField(FormMetadata form, FormField field) {
        Node node = new Node();
        node.form = form;
        node.field = field;
        node.path = new FieldPath(field.getId());
        rootFields.add(node);
        nodeMap.put(node.path, node);
        return node;
    }

    public void addFormMetadata(FormMetadata form) {
        assert form != null;
        if (!formMap.containsKey(form.getId())) {
            formMap.put(form.getId(), form);
        }
    }

    public List<Node> getRootFields() {
        return rootFields;
    }


    public boolean hasVisibleSubForms() {
        for (Node node : getRootFields()) {
            if(node.isSubForm() && node.isSubFormVisible()) {
                return true;
            }
        }
        return false;
    }

    public List<ColumnNode> getColumnNodes() {
        List<ColumnNode> columns = Lists.newArrayList();
        Map<ResourceId, ColumnNode> columnMap = Maps.newHashMap();

        enumerateColumns(getRootFields(), columns, columnMap);
        return columns;
    }

    private void enumerateColumns(List<FormTree.Node> fields, List<ColumnNode> columns, Map<ResourceId, ColumnNode> columnMap) {


        for (FormTree.Node node : fields) {

            if (node.getType() instanceof SubFormReferenceType) { // skip subForm fields
                continue;
            }

            if (node.isReference()) {
                enumerateColumns(node.getChildren(), columns, columnMap);

            } else if(node.getType() instanceof GeoPointType) {

            } else {
                if (!columnMap.containsKey(node.getFieldId())) {
                    ColumnNode col = new ColumnNode(node);
                    columnMap.put(node.getFieldId(), col);
                    columns.add(col);
                }
            }
        }
    }

    public List<FieldPath> getRootPaths() {
        List<FieldPath> paths = Lists.newArrayList();
        for (Node node : rootFields) {
            paths.add(node.getPath());
        }
        return paths;
    }


    public FormClass getRootFormClass() {
        return formMap.get(rootFormId).getSchema();
    }


    public FormMetadata getRootMetadata() {
        return formMap.get(rootFormId);
    }

    public FormClass getFormClass(ResourceId formClassId) {
        Optional<FormClass> formClass = getFormClassIfPresent(formClassId);
        if(!formClass.isPresent()) {
            throw new IllegalArgumentException("No such FormClass: " + formClassId);
        }
        return formClass.get();
    }


    public FormMetadata getFormMetadata(ResourceId formId) {
        FormMetadata metadata = formMap.get(formId);
        if(metadata == null) {
            return FormMetadata.notFound(formId);
        }
        return metadata;
    }

    public Collection<FormMetadata> getForms() {
        return formMap.values();
    }

    public Optional<FormClass> getFormClassIfPresent(ResourceId formId) {
        return Optional.fromNullable(getFormMetadata(formId).getSchema());
    }

    public Node getNodeByPath(FieldPath path) {
        Node node = nodeMap.get(path);
        if (node == null) {
            throw new IllegalArgumentException();
        }
        return node;
    }


    public Node getRootField(ResourceId fieldId) {
        return nodeMap.get(new FieldPath(fieldId));
    }

    private void findLeaves(List<Node> leaves, Iterable<Node> children) {
        for(Node child : children) {
            if(child.isLeaf()) {
                leaves.add(child);
            } else {
                findLeaves(leaves, child.getChildren());
            }
        }
    }

    public List<Node> getLeaves() {
        List<Node> leaves = Lists.newArrayList();
        findLeaves(leaves, rootFields);
        return leaves;
    }

    public List<FieldPath> search(SearchOrder order, Predicate<? super Node> descendPredicate,
                                  Predicate<? super Node> matchPredicate) {
        List<FieldPath> paths = Lists.newArrayList();
        search(paths, rootFields, order, descendPredicate, matchPredicate);
        return paths;
    }

    public List<FieldPath> search(SearchOrder order, Node parent) {
        List<FieldPath> paths = Lists.newArrayList();
        search(paths, parent.getChildren(), order, Predicates.alwaysTrue(), Predicates.alwaysTrue());
        return paths;
    }

    private void search(List<FieldPath> paths,
                        Iterable<Node> childNodes,
                        SearchOrder searchOrder,
                        Predicate<? super Node> descendPredicate,
                        Predicate<? super Node> matchPredicate) {

        for(Node child : childNodes) {

            if (searchOrder == SearchOrder.BREADTH_FIRST && matchPredicate.apply(child)) {
                paths.add(child.path);
            }

            if(!child.getChildren().isEmpty() && descendPredicate.apply(child)) {
                search(paths, child.getChildren(), searchOrder, descendPredicate, matchPredicate);
            }

            if (searchOrder == SearchOrder.DEPTH_FIRST && matchPredicate.apply(child)) {
                paths.add(child.path);
            }
        }
    }

    public static Predicate<Node> isDataTypeProperty() {
        return new Predicate<FormTree.Node>() {

            @Override
            public boolean apply(Node input) {
                return input.field.getType() instanceof ReferenceType;
            }
        };
    }

    public static Predicate<Node> isReference() {
        return Predicates.not(isDataTypeProperty());
    }

    public static Predicate<Node> pathIn(final Collection<FieldPath> paths) {
        return new Predicate<FormTree.Node>() {

            @Override
            public boolean apply(Node input) {
                return paths.contains(input.path);
            }
        };
    }

    public static Predicate<Node> pathNotIn(final Collection<FieldPath> paths) {
        return Predicates.not(pathIn(paths));
    }

    public FormTree subTree(ResourceId formId) {
        if (formId.equals(this.rootFormId)) {
            return this;
        }
        FormTreeBuilder treeBuilder = new FormTreeBuilder((FormMetadataProvider)this);
        return treeBuilder.queryTree(formId);
    }

    public FormTree parentTree() {
        FormClass rootFormClass = getRootFormClass();
        assert rootFormClass.isSubForm();

        ResourceId parentFormId = rootFormClass.getParentFormId().get();
        return subTree(parentFormId);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(Node node : getLeaves()) {
            s.append(node.debugPath()).append("\n");
        }
        return s.toString();
    }
}
