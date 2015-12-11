package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.expr.CalculatedFieldType;

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
    private FormTree.Node field;
    private Type type;

    private NodeMatch() {}
    

    /**
     * Creates a NodeMatch for the given field.
     */
    public static NodeMatch forField(FormTree.Node fieldNode) {
        Preconditions.checkNotNull(fieldNode, "fieldNode");
        
        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(fieldNode);
        match.type = Type.FIELD;
        match.formClass = fieldNode.getDefiningFormClass();
        match.field = fieldNode;
        return match;
    }

    public static NodeMatch forId(FormTree.Node parent, FormClass formClass) {
        NodeMatch match = new NodeMatch();
        match.joins = joinsTo(parent);
        match.joins.add(new JoinNode(parent, formClass.getId()));
        match.formClass = formClass;
        match.type = Type.ID;
        return match;
    }

    private static List<JoinNode> joinsTo(FormTree.Node node) {
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
        while(node.getParent() != null) {
            joins.addFirst(new JoinNode(
                    node.getParent(),                         // Parent Field ->
                    node.getDefiningFormClass().getId()));     // This Field's FormClass
            node = node.getParent();
        }
        return joins;
    }

    public List<JoinNode> getJoins() {
        return joins;
    }

    public boolean isRoot() {
        return field.isRoot();
    }

    public FormTree.Node getField() {
        Preconditions.checkArgument(type == Type.FIELD);
        return field;
    }

    public Type getType() {
        return type;
    }

    public boolean isCalculated() {
        return type == Type.FIELD && field.isCalculated();
    }

    public String getCalculation() {
        if(!isCalculated()) {
            throw new UnsupportedOperationException(field + " is not a calculated field");
        }
        CalculatedFieldType type = (CalculatedFieldType) field.getField().getType();
        return type.getExpressionAsString();
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
            s.append(join.getReferenceField().getFieldId());
            s.append('.');
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
                s.append(field.getField().getId());
                break;
        }
        return s.toString();
    }
    
    @Override
    public String toString() {
        String s = field.debugPath();
        if(type == Type.ID) {
            s += "." + formClass.getId() + ":" + "@id";
        }
        return s;
    }
}
