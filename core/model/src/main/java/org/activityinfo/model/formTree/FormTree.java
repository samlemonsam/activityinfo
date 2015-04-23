package org.activityinfo.model.formTree;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.ReferenceType;

import java.util.*;

/**
 * Contains a tree of fields based on references to other {@code FormClasses}
 */
public class FormTree {


    public class Node {

        private Node parent;
        private FormField field;

        private FieldPath path;
        private FormClass formClass;
        private List<Node> children = Lists.newArrayList();
        private int depth;

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isReference() {
            return field.getType() instanceof ReferenceType;
        }

        public Node addChild(FormClass declaringClass, FormField field) {
            assert isReference() : "only reference fields can have children";

            FormTree.Node childNode = new FormTree.Node();
            childNode.parent = this;
            childNode.field = field;
            childNode.path = new FieldPath(this.path, field.getId());
            childNode.formClass = declaringClass;
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
            return formClass;
        }

        public ResourceId getFieldId() {
            return field.getId();
        }

        /**
         *
         * @return for Reference fields, the range of this field
         */
        public Set<ResourceId> getRange() {
            if(field.getType() instanceof ReferenceType) {
                return ((ReferenceType) field.getType()).getRange();
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


    }

    public enum SearchOrder {
        DEPTH_FIRST,
        BREADTH_FIRST
    }

    private List<Node> rootFields = Lists.newArrayList();
    private Map<FieldPath, Node> nodeMap = Maps.newHashMap();

    public FormTree() {

    }

    public Node addRootField(FormClass declaringClass, FormField field) {
        Node node = new Node();
        node.formClass = declaringClass;
        node.field = field;
        node.path = new FieldPath(field.getId());
        rootFields.add(node);
        nodeMap.put(node.path, node);
        return node;
    }

    public List<Node> getRootFields() {
        return rootFields;
    }

    public List<FieldPath> getRootPaths() {
        List<FieldPath> paths = Lists.newArrayList();
        for (Node node : rootFields) {
            paths.add(node.getPath());
        }
        return paths;

    }

    public Map<ResourceId, FormClass> getRootFormClasses() {
        Map<ResourceId, FormClass> map = Maps.newHashMap();
        for(Node node : rootFields) {
            map.put(node.getDefiningFormClass().getId(), node.getDefiningFormClass());
        }
        return map;
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

    private void search(List<FieldPath> paths,
                        Iterable<Node> childNodes,
                        SearchOrder searchOrder,
                        Predicate<? super Node> descendPredicate,
                        Predicate<? super Node> matchPredicate) {

        for(Node child : childNodes) {

            if (searchOrder == SearchOrder.BREADTH_FIRST && matchPredicate.apply(child)) {
                paths.add(child.path);
            }

            if(!child.getChildren().isEmpty() & descendPredicate.apply(child)) {
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


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(Node node : getLeaves()) {
            s.append(node.debugPath()).append("\n");
        }
        return s.toString();
    }
}
