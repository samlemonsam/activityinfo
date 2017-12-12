package org.activityinfo.store.query.shared.plan;

import com.google.common.base.Preconditions;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.store.query.shared.join.JoinNode;

import java.util.List;

/**
 * Defines a sequence of joins from the base form to the data column
 */
public class FieldPlanNode extends PlanNode {

    private ResourceId formId;
    private FormField field;

    public FieldPlanNode(ResourceId formId, FormField field) {
        this.formId = formId;
        this.field = field;
    }


    /**
     * Creates a NodeMatch for the given field.
     */
    public static PlanNode forField(FormTree.Node fieldNode) {

        FieldPlanNode planNode = new FieldPlanNode(fieldNode.getDefiningFormClass().getId(), fieldNode.getField());

        return maybeJoin(fieldNode, planNode);
    }

    private static PlanNode maybeJoin(FormTree.Node rightNode, PlanNode rightPlanNode) {

        if(rightNode.isRoot()) {
            return rightPlanNode;
        } else {
            return join(rightNode.getParent(), rightNode.getDefiningFormClass().getId(), rightPlanNode);
        }
    }

    private static PlanNode join(FormTree.Node parentNode, ResourceId rightFormId, PlanNode rightPlanNode) {
        if(parentNode.getType() instanceof ReferenceType) {

            // Joining from a reference field to an expression on the referenced form
            // = MANY-TO-ONE

            FormTree.Node leftField = parentNode;
            FormClass leftForm = parentNode.getDefiningFormClass();
            ReferencePlanNode leftPlanNode = new ReferencePlanNode(
                    leftForm.getId(),
                    leftField.getField(),
                    rightFormId,
                    rightPlanNode);

            return maybeJoin(leftField, leftPlanNode);

        } else if(parentNode.isSubForm()) {

            // Joining from a parent form to a subform expression.
            // MANY-TO-MANY

            FormTree.Node subFormField = parentNode;
            FormClass parentForm = subFormField.getDefiningFormClass();

            SubFormNode leftPlanNode = new SubFormNode(
                    parentForm.getId(),
                    subFormField.getField(),
                    rightPlanNode);


            return maybeJoin(subFormField, leftPlanNode);

        } else {
            throw new IllegalArgumentException("parentNode is not a reference or subform field");
        }
    }


    public static PlanNode forEnumItem(FormTree.Node fieldNode, EnumItem item) {
        return new EnumNode(forField(fieldNode), item);
    }
    
    public static PlanNode forId(FormTree.Node parent, FormClass formClass) {
        return join(parent, formClass.getId(), new RecordIdNode(formClass.getId()));
    }

    public ExprNode getExpr() {
        Preconditions.checkArgument(type == Type.FIELD, FieldPlanNode.class.getName() + " is of type " + type);
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

    public FormTree.Node getFieldNode() {
        return fieldNode;
    }

    public boolean isEnumBoolean() {
        return enumItem != null;
    }

    public EnumItem getEnumItem() {
        return enumItem;
    }

    @Override
    public FieldType getFieldType() {
        return fieldNode.getType();
    }

    @Override
    public <T> T accept(PlanVisitor<T> visitor) {
        return visitor.visitFieldNode(this);
    }

    @Override
    public String toString() {
        return fieldNode.debugPath();
    }

    public ResourceId getFormId() {
        return getFormClass().getId();
    }
}
