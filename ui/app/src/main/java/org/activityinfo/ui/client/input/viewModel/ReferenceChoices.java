package org.activityinfo.ui.client.input.viewModel;

import com.google.common.collect.Iterables;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Choices for a single ReferenceType field.
 *
 * <p>There may be a hierarchy of choices.</p>
 */
public class ReferenceChoices {

    private final Observable<ReferenceChoiceSet> choices;

    public ReferenceChoices(FormStore formStore, FormTree formTree, ReferenceType referenceType) {
        ResourceId formId = Iterables.getOnlyElement(referenceType.getRange());
        FormClass refFormClass = formTree.getFormClass(formId);
        ExprNode labelExpr = refFormClass.findLabelExpression();

        QueryModel queryModel = new QueryModel(formId);
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr(labelExpr).as("label");

        this.choices = formStore.query(queryModel).transform(columnSet -> new ReferenceChoiceSet(formId, columnSet));
    }

    public Observable<ReferenceChoiceSet> getChoices() {
        return choices;
    }
}
