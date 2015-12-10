package org.activityinfo.store.query.impl.eval;

import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.expr.CalculatedFieldType;

/**
 * Defines a sequence of joins from the base form to the data column
 */
public class NodeMatch {



    public enum Type {
        ID,
        CLASS,
        FIELD
    }

    private final FormTree.Node node;
    private final ResourceId formClassId;
    private final Type type;


    public NodeMatch(FormTree.Node node) {
        Preconditions.checkNotNull(node, "node");
        
        this.node = node;
        this.formClassId = null;
        this.type = Type.FIELD;
    }

    private NodeMatch(FormTree.Node node, FormClass formClass, Type type) {
        Preconditions.checkNotNull(node, "node");

        this.node = node;
        this.type = type;
        this.formClassId = formClass.getId();
    }
    
    public static NodeMatch id(FormTree.Node node, FormClass formClass) {
        return new NodeMatch(node, formClass, Type.ID);
    }

    public boolean isRoot() {
        return node.isRoot();
    }

    public FormTree.Node getNode() {
        return node;
    }

    public Type getType() {
        return type;
    }

    public boolean isCalculated() {
        return type == Type.FIELD && node.isCalculated();
    }

    public String getCalculation() {
        if(!isCalculated()) {
            throw new UnsupportedOperationException(node + " is not a calculated field");
        }
        CalculatedFieldType type = (CalculatedFieldType) node.getField().getType();
        return type.getExpressionAsString();
    }
    
    public ResourceId getFormClassId() {
        return formClassId;
    }
    
    @Override
    public String toString() {
        String s = node.debugPath();
        if(type == Type.ID) {
            s += "." + formClassId + ":" + "@id";
        }
        return s;
    }
}
