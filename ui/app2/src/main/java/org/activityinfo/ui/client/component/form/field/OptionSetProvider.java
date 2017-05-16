package org.activityinfo.ui.client.component.form.field;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;

/**
 * Provides a list of options for a referenc feield?
 */
public class OptionSetProvider {

    private ResourceLocator resourceLocator;

    public OptionSetProvider(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
    }


    public Promise<OptionSet> queryOptionSet(ReferenceType referenceType) {
        final ResourceId formId = Iterables.getOnlyElement(referenceType.getRange());

        return resourceLocator.getFormClass(formId).join(new Function<FormClass, Promise<OptionSet>>() {
            @Override
            public Promise<OptionSet> apply(FormClass formClass) {

                QueryModel queryModel = new QueryModel(formId);
                queryModel.selectResourceId().as("id");
                queryModel.selectExpr(formClass.findLabelExpression()).as("label");

                return resourceLocator
                    .queryTable(queryModel)
                    .then(new Function<ColumnSet, OptionSet>() {

                        @Nullable
                        @Override
                        public OptionSet apply(@Nullable ColumnSet columnSet) {
                            return new OptionSet(formId, columnSet);
                        }
                    });
            }
        });

    }



}
