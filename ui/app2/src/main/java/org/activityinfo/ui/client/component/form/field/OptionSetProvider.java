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
package org.activityinfo.ui.client.component.form.field;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.SerialNumberType;
import org.activityinfo.model.type.primitive.TextType;
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
        return resourceLocator.getFormTree(formId).join(new Function<FormTree, Promise<OptionSet>>() {
            @Override
            public Promise<OptionSet> apply(FormTree formTree) {

                QueryModel queryModel = new QueryModel(formId);
                queryModel.selectResourceId().as("id");
                queryModel.selectExpr(findLabelExpression(formTree.getRootFields())).as("label");

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

    private FormulaNode findLabelExpression(Iterable<FormTree.Node> fieldNodes) {
        for (FormTree.Node node : fieldNodes) {
            if(node.getField().isKey()) {
                if(node.isReference()) {
                    return findLabelExpression(node.getChildren());
                } else {
                    return node.getPath().toExpr();
                }
            }
        }

        // If there are no keys expicitly defined, then
        // look for a field with the "label" tag OR a serial number
        for (FormTree.Node node : fieldNodes) {
            if(node.getField().getSuperProperties().contains(ResourceId.valueOf("label")) ||
                node.getField().getType() instanceof SerialNumberType) {
                return node.getPath().toExpr();
            }
        }


        // If no such field exists, pick the first text field
        for (FormTree.Node node : fieldNodes) {
            if(node.getType() instanceof TextType) {
                return node.getPath().toExpr();
            }
        }

        // Otherwise fall back to the generated id
        return new SymbolNode(ColumnModel.ID_SYMBOL);
    }


}
