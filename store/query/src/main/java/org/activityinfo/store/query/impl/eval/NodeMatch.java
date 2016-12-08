package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

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

    private List<JoinNode> joins;
    private FormClass formClass;
    private ExprNode fieldExpr;
    private FormTree.Node fieldNode;
    private Type type;

    private NodeMatch() {}
    

    /**
     * Creates a NodeMatch for the given field.
     */
    public static NodeMatch forField(FormTree.Node fieldNode) {
        Preconditions.checkNotNull(fieldNode, "fieldNode");

        List<List<FormTree.Node>> partitions = partitionOnJoins(fieldNode);
        List<FormTree.Node> leaf = partitions.get(partitions.size() - 1);
        
        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(partitions);
        match.type = Type.FIELD;
        match.formClass = leaf.get(0).getDefiningFormClass();
        match.fieldExpr = toExpr(leaf);
        match.fieldNode = fieldNode;
        return match;
    }

    public static NodeMatch forEnumItem(FormTree.Node fieldNode, EnumItem item) {
        NodeMatch match = forField(fieldNode);
        match.fieldExpr = new CompoundExpr(match.fieldExpr, new SymbolExpr(item.getId()));
        
        return match;
    }
    
    public static NodeMatch forId(FormTree.Node parent, FormClass formClass) {

        List<List<FormTree.Node>> partitions = partitionOnJoins(parent);
        
        List<FormTree.Node> leaf = partitions.get(partitions.size() - 1);
        
        // Embedded records are not independent resources, and so 
        // do not have their own ID. So the leaf field MUST be a reference field
        Preconditions.checkArgument(leaf.get(0).isReference());
        
        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(partitions);
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

    private static List<JoinNode> joinsTo(List<List<FormTree.Node>> partitions) {
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
            ExprNode leftFieldExpr = toExpr(left);

            // "RIGHT" side
            // Joining fom left to right using resource ids (primary key)
            List<FormTree.Node> right = partitions.get(i+1);
            ResourceId rightFormId = right.get(0).getDefiningFormClass().getId();

            if(leftField.getType() instanceof ReferenceType) {
                // Join based on the (left) foreign key ==> (right) primary key
                joins.add(new JoinNode(JoinType.REFERENCE, leftFormId, leftFieldExpr, rightFormId));

            } else if(leftField.getType() instanceof SubFormReferenceType) {
                joins.add(new JoinNode(JoinType.SUBFORM, leftFormId, new SymbolExpr(ColumnModel.ID_SYMBOL), rightFormId));

            } else {
                throw new IllegalStateException("Invalid field for joining: " + leftField.getType());
            }
        }
        
        return joins;
    }

    private static ExprNode toExpr(List<FormTree.Node> partition) {
        Iterator<FormTree.Node> it = partition.iterator();
        ExprNode expr = new SymbolExpr(it.next().getFieldId());
        while(it.hasNext()) {
            expr = new CompoundExpr(expr, new SymbolExpr(it.next().getFieldId()));
        }
        return expr;
    }

    public List<JoinNode> getJoins() {
        return joins;
    }

    public boolean isRoot() {
        return fieldNode.isRoot();
    }

    public ExprNode getExpr() {
        Preconditions.checkArgument(type == Type.FIELD, NodeMatch.class.getName() + " is of type " + type);
        return fieldExpr;
    }

    public Type getType() {
        return type;
    }

    public boolean isCalculated() {
        return type == Type.FIELD && fieldNode.isCalculated();
    }

    public String getCalculation() {
        if(!isCalculated()) {
            throw new UnsupportedOperationException(fieldExpr + " is not a calculated field");
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
                s.append(fieldExpr.toString());
                break;
        }
        return s.toString();
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
