package org.activityinfo.ui.client.analysis.viewModel;

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

import com.google.common.base.Function;
import org.activityinfo.ui.client.analysis.model.Axis;

import java.util.*;
import java.util.Map.Entry;

public class PivotTable {

    private Node rootRow = new Node();
    private Node rootColumn = new Node();

    private List<EffectiveDimension> rowDimensions;
    private List<EffectiveDimension> columnDimensions;

    public PivotTable() {
        this.rowDimensions = Collections.emptyList();
        this.columnDimensions = Collections.emptyList();
    }

    public PivotTable(AnalysisResult results) {
        this.rowDimensions = results.getEffectiveModel().getDimensions(Axis.ROW);
        this.columnDimensions = results.getEffectiveModel().getDimensions(Axis.COLUMN);

        for (Point point : results.getPoints()) {
            PivotTable.Node column = columnDimensions.isEmpty() ? rootColumn : find(rootColumn,
                    columnDimensions.iterator(),
                    point);
            PivotTable.Node row = rowDimensions.isEmpty() ? rootRow : find(rootRow,
                    rowDimensions.iterator(),
                    point);

            row.setValue(column, point);
        }
    }

    public List<EffectiveDimension> getRowDimensions() {
        return rowDimensions;
    }

    public List<EffectiveDimension> getColumnDimensions() {
        return columnDimensions;
    }

    private Node find(Node parent, Iterator<EffectiveDimension> dimensionIterator, Point point) {

        EffectiveDimension childDimension = dimensionIterator.next();
        Function<Point, String> categoryProvider = childDimension.getCategoryProvider();

        if(parent.dimension == null) {
            parent.dimension = childDimension;
        }

        Comparator<String> categoryComparator = childDimension.getCategoryComparator();

        String category = categoryProvider.apply(point);
        PivotTable.Node child = parent.getChild(category);
        if (child == null) {
            child = parent.addChild(
                    category,
                    categoryComparator);
        }
        if (dimensionIterator.hasNext()) {
            return find(child, dimensionIterator, point);
        } else {
            return child;
        }
    }

    public boolean isEmpty() {
        return rootRow.isLeaf() && rootColumn.isLeaf();
    }

    public Node getRootRow() {
        return rootRow;
    }

    public Node getRootColumn() {
        return rootColumn;
    }

    public Node getRootCategory() {
        return getRootRow();
    }

    public Node getRootSeries() {
        return getRootColumn();
    }
    
    public static List<String> flattenLabels(List<Node> list) {
        List<String> labels = new ArrayList<>();
        for (Node node : list) {
            labels.add(node.flattenLabel());
        }
        return labels;
    }

    public static class Node {

        private Node parent;
        private EffectiveDimension dimension;
        private String category;

        private Map<String, Node> childMap = new HashMap<>();
        private Map<Node, Point> cells = new HashMap<>();

        private List<Node> children = new ArrayList<>();

        private Node() {

        }

        private Node(Node parent, String category) {
            this.parent = parent;
            this.category = category;
        }

        public Node getChild(String category) {
            return childMap.get(category);
        }

        public Node addChild(String category, Comparator<String> comparator) {

            Node child = new Node(this, category);

            childMap.put(category, child);

            if (comparator == null) {
                children.add(child);
            } else {
                insertChildSorted(child, comparator);
            }
            return child;
        }

        private void insertChildSorted(Node child, Comparator<String> comparator) {
            for (int i = 0; i != children.size(); ++i) {
                if (comparator.compare(child.category, children.get(i).category) < 0) {
                    children.add(i, child);
                    return;
                }
            }
            children.add(child);
        }

        public Node nextSibling() {
            if (parent == null) {
                return null;
            }

            int i = parent.children.indexOf(this);

            if (i < 1) {
                return null;
            } else {
                return parent.children.get(i - 1);
            }
        }

        public Node prevSibling() {
            if (parent == null) {
                return null;
            }

            int i = parent.children.indexOf(this);

            if (i == parent.children.size() - 1) {
                return null;
            } else {
                return parent.children.get(i + 1);
            }
        }

        /**
         * @return A list of all terminal (leaf) descendant nodes
         */
        public List<Node> getLeaves() {
            List<Node> list = new ArrayList<>();
            if (isLeaf()) {
                list.add(this);
            } else {
                findLeaves(list);
            }
            return list;
        }

        private void findLeaves(List<Node> list) {
            for (Node child : getChildren()) {
                if (child.isLeaf()) {
                    list.add(child);
                } else {
                    child.findLeaves(list);
                }
            }
        }

        public Node firstChild() {
            return children.get(0);
        }

        public Node lastChild() {
            return children.get(children.size() - 1);
        }

        private void setValue(Node column, Point value) {
            cells.put(column, value);
        }

        public Point getPoint(Node column) {
            return cells.get(column);
        }

        public EffectiveDimension getDimension() {
            return dimension;
        }

        public String getCategory() {
            return category;
        }

        public String getCategoryLabel() {
            if(Point.TOTAL.equals(category)) {
                return parent.getDimension().getTotalLabel();
            } else {
                return category;
            }
        }

        public Map<Node, Point> getPoints() {
            return cells;
        }

        public int getChildCount() {
            return childMap.size();
        }

        public Node getParent() {
            return parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public String flattenLabel() {
            StringBuilder sb = new StringBuilder();
            Node node = this;
            do {
                if (node.getCategoryLabel() != null) {
                    if (sb.length() != 0) {
                        sb.append(" ");
                    }

                    sb.append(node.getCategoryLabel());
                }
                node = node.getParent();

            } while (node != null);

            return sb.toString();
        }

        public void appendString(int depth, StringBuilder sb) {
            for (int i = 0; i != depth; ++i) {
                sb.append("  ");
            }
            sb.append(dimension).append(":").append(category);

            for (Entry<Node, Point> column : cells.entrySet()) {
                sb.append(" | ");
                sb.append(column.getKey().category).append("=").append(column.getValue().getValue());
            }
            sb.append("\n");
            for (Node child : getChildren()) {
                child.appendString(depth + 1, sb);
            }

        }
        public boolean isLeaf() {
            return children.isEmpty();
        }

        public int getDepth() {
            return calculateDepth(0);
        }

        protected int calculateDepth(int depth) {
            int maxChildDepth = depth;
            for (Node child : getChildren()) {
                int childDepth = child.calculateDepth(depth + 1);
                if (maxChildDepth < childDepth) {
                    maxChildDepth = childDepth;
                }
            }
            return maxChildDepth;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(" COLUMNS:\n");
        for (Node col : rootColumn.getChildren()) {
            col.appendString(1, sb);
        }
        sb.append(" ROWS:\n");
        for (Node row : rootRow.getChildren()) {
            row.appendString(1, sb);
        }
        return sb.toString();
    }
}
