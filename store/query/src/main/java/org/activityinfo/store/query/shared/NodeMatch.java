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
package org.activityinfo.store.query.shared;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.StatFunction;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.store.query.shared.join.JoinNode;
import org.activityinfo.store.query.shared.join.JoinType;
import org.activityinfo.store.spi.FieldComponent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines a sequence of joins from the base form to the data column
 */
public class NodeMatch {



    public enum Type {
        ID,
        CLASS,
        FIELD
    }

    private Type type;
    private List<JoinNode> joins;
    private FormClass formClass;
    private FormTree.Node fieldNode;
    private FieldComponent fieldComponent;

    private NodeMatch() {}
    

    /**
     * Creates a NodeMatch for the given field.
     */
    public static NodeMatch forField(FormTree.Node fieldNode, Optional<StatFunction> aggregation) {
        Preconditions.checkNotNull(fieldNode, "fieldNode");

        List<List<FormTree.Node>> partitions = partitionOnJoins(fieldNode);
        List<FormTree.Node> leaf = partitions.get(partitions.size() - 1);

        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(partitions, aggregation);
        match.type = Type.FIELD;
        match.formClass = leaf.get(0).getDefiningFormClass();
        match.fieldNode = fieldNode;
        match.fieldComponent = new FieldComponent(fieldNode.getField().getName());

        FormulaNode leafFormula = toExpr(leaf);
        if(!(leafFormula instanceof SymbolNode)) {
            throw new UnsupportedOperationException("TODO: " + leafFormula);
        }

        return match;
    }

    public static NodeMatch forFieldComponent(FormTree.Node fieldNode, String component) {
        NodeMatch match = forField(fieldNode, Optional.<StatFunction>absent());
        match.fieldComponent = new FieldComponent(fieldNode.getFieldId(), component);
        return match;
    }

    public static NodeMatch forId(String idSymbol, FormClass formClass) {
        NodeMatch match = new NodeMatch();
        match.formClass = formClass;
        match.type = Type.ID;
        match.joins = Lists.newLinkedList();
        return match;
    }

    public boolean isRootId() {
        // only return true for unjoined ID types - i.e. root form/record ids
        return type == Type.ID && joins.isEmpty();
    }
    
    public static NodeMatch forId(FormTree.Node parent, FormClass formClass) {

        List<List<FormTree.Node>> partitions = partitionOnJoins(parent);
        
        List<FormTree.Node> leaf = partitions.get(partitions.size() - 1);
        
        // Embedded records are not independent resources, and so 
        // do not have their own ID. So the leaf field MUST be a reference field
        Preconditions.checkArgument(leaf.get(0).isReference());
        
        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(partitions, Optional.<StatFunction>absent());
        match.joins.add(new JoinNode(JoinType.REFERENCE, leaf.get(0).getDefiningFormClass().getId(), toExpr(leaf), formClass.getId()));
        match.formClass = formClass;
        match.type = Type.ID;
        return match;
    }

    /**
     * For a compound expression like A.B, A can <em>either</em> to a reference field or 
     * a record-valued field like a geographic point. However, we need to join <em>only</em> on 
     * reference fields. 
     * 
     * <p>This routine partitions a field path on the links for which joins must be made, that is,
     * across reference fields.</p>
     * 
     * <p>So if you have the expression Site.Village.Point.Latitude, where Site and Village are reference
     * fields, but Point is an embedded record field, then the result would be {Site, Village, Point.Latitude}</p>
     */
    private static List<List<FormTree.Node>> partitionOnJoins(FormTree.Node node) {
        LinkedList<List<FormTree.Node>> partitions = Lists.newLinkedList();
        
        while(node != null) {

            LinkedList<FormTree.Node> current = Lists.newLinkedList();
            current.add(node);
            
            // include all non-reference ancestors in this partition
            while(node.getParent() != null && !node.getParent().isReference()) {
                current.addFirst(node.getParent());
                node = node.getParent();
            }
            
            partitions.addFirst(current);
            
            node = node.getParent();
        }
        return partitions;
    }

    private static List<JoinNode> joinsTo(List<List<FormTree.Node>> partitions, Optional<StatFunction> aggregation) {
        /*
         *  Given a parent: "Site.Location.Territoire.District"
         *  This is represented as a tree of nodes:
         *      District -> Territoire -> Location -> Site
         *      
         *  We want to turn into a list of joins: 
         *      (site field -> form site), 
         *      (location field -> form school),
         *      (field territoire -> form Territoire)
         *      (field district -> form District)
         */

        LinkedList<JoinNode> joins = new LinkedList<>();
        for(int i = 0; i < partitions.size() - 1; i++) {
            // Reference field that functions as a foreign key
            List<FormTree.Node> left = partitions.get(i);
            FormField leftField = left.get(0).getField();
            ResourceId leftFormId = left.get(0).getDefiningFormClass().getId();
            FormulaNode leftFieldExpr = toExpr(left);

            // "RIGHT" side
            // Joining fom left to right using resource ids (primary key)
            List<FormTree.Node> right = partitions.get(i+1);
            ResourceId rightFormId = right.get(0).getDefiningFormClass().getId();

            if(leftField.getType() instanceof ReferenceType) {
                // Join based on the (left) foreign key ==> (right) primary key
                joins.add(new JoinNode(JoinType.REFERENCE, leftFormId, leftFieldExpr, rightFormId,
                        Optional.<StatFunction>absent()));

            } else if(leftField.getType() instanceof SubFormReferenceType) {
                joins.add(new JoinNode(
                        JoinType.SUBFORM,
                                leftFormId,
                                new SymbolNode(ColumnModel.ID_SYMBOL),
                                rightFormId,
                                aggregation));

            } else {
                throw new IllegalStateException("Invalid field for joining: " + leftField.getType());
            }
        }
        
        return joins;
    }

    private static FormulaNode toExpr(List<FormTree.Node> partition) {
        Iterator<FormTree.Node> it = partition.iterator();
        FormulaNode expr = new SymbolNode(it.next().getFieldId());
        while(it.hasNext()) {
            expr = new CompoundExpr(expr, new SymbolNode(it.next().getFieldId()));
        }
        return expr;
    }

    public NodeMatch withComponent(String component) {
        NodeMatch nodeMatch = new NodeMatch();
        nodeMatch.type = type;
        nodeMatch.joins = joins;
        nodeMatch.formClass = formClass;
        nodeMatch.fieldNode = fieldNode;
        nodeMatch.fieldComponent = new FieldComponent(fieldComponent.getFieldId(), component);
        return nodeMatch;
    }

    public List<JoinNode> getJoins() {
        return joins;
    }

    public boolean isRoot() {
        return fieldNode.isRoot();
    }

    public Type getType() {
        return type;
    }

    public boolean isCalculated() {
        return type == Type.FIELD && fieldNode.isCalculated();
    }

    public String getCalculation() {
        if(!isCalculated()) {
            throw new UnsupportedOperationException(getFieldComponent() + " is not a calculated field");
        }
        CalculatedFieldType type = (CalculatedFieldType) fieldNode.getField().getType();
        return type.getExpression();
    }

    public FormClass getFormClass() {
        return formClass;
    }


    public boolean isJoined() {
        return !joins.isEmpty();
    }

    public String toDebugString() {
        StringBuilder s = new StringBuilder();
        for (JoinNode join : joins) {
            s.append(join.getReferenceField());
            s.append('>');
        }
        switch (type) {
            case ID:
                s.append(formClass.getId());
                s.append("@id");
                break;
            case CLASS:
                s.append(formClass.getId());
                s.append("@class");
                break;
            case FIELD:
                s.append(fieldComponent);
                break;
        }
        return s.toString();
    }

    public FormTree.Node getFieldNode() {
        return fieldNode;
    }

    public boolean isEnumBoolean() {
        return fieldNode != null && fieldNode.getType() instanceof EnumType && fieldComponent.hasComponent();
    }

    public EnumItem getEnumItem() {
        EnumType enumType = (EnumType) fieldNode.getType();
        for (EnumItem enumItem : enumType.getValues()) {
            if(enumItem.getId().asString().equals(fieldComponent.getComponent())) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("No such item " + fieldComponent.getComponent());
    }

    public FieldComponent getFieldComponent() {
        return fieldComponent;
    }

    public String getDescription() {
        return fieldNode.getField().getLabel();
    }

    @Override
    public String toString() {
        if(type == Type.ID) {
            return formClass.getId() + ":" + "@id";
        } else {
            return fieldNode.debugPath();
        }
    }
}
