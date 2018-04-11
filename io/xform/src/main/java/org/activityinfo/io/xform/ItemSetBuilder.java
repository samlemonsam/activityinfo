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
package org.activityinfo.io.xform;

import com.google.common.collect.Iterables;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.store.query.shared.FormSource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Constructs an itemset for all the reference fields in a form.
 */
public class ItemSetBuilder {

    private final FormSource formSource;
    private final ItemSetWriter writer;
    private final int keyCount;

    public ItemSetBuilder(FormSource formSource, ResourceId formId, OutputStream output) throws IOException {
        this.formSource = formSource;

        FormTree formTree = formSource.getFormTree(formId).waitFor();
        Map<FormField, LookupKeySet> keySets = new HashMap<>();
        for (FormTree.Node node : formTree.getRootFields()) {
            if (node.getType() instanceof ReferenceType) {
                LookupKeySet lookupKeySet = new LookupKeySet(formTree, node.getField());
                keySets.put(node.getField(), lookupKeySet);
            }
        }

        keyCount = keySets.values()
                .stream()
                .mapToInt(keySet -> keySet.getLookupKeys().size())
                .max()
                .orElse(0);


        writer = new ItemSetWriter(output, keyCount);


        // We need at least one item set or ODK will crash
        writer.writeItem("__dummy", "dummy", "dummy");

        for (Map.Entry<FormField, LookupKeySet> field : keySets.entrySet()) {
            ReferenceType type = (ReferenceType) field.getKey().getType();
            if(type.getRange().size() == 1) {
                writeKeys(field.getKey(), Iterables.getOnlyElement(type.getRange()), field.getValue());
            }
        }
        writer.flush();
    }

    private void writeKeys(FormField field, ResourceId referencedFormId, LookupKeySet lookupKeySet) throws IOException {

        LookupKey leafKey = lookupKeySet.getLeafKey(referencedFormId);

        QueryModel queryModel = new QueryModel(referencedFormId);
        queryModel.selectResourceId().as("id");

        for (Map.Entry<LookupKey, FormulaNode> entry : leafKey.getKeyFormulas().entrySet()) {
            queryModel.selectExpr(entry.getValue()).as("k" + entry.getKey().getKeyIndex());
        }

        ColumnSet columnSet = formSource.query(queryModel).waitFor();
        ColumnView id = columnSet.getColumnView("id");
        ColumnView[] keyColumns = new ColumnView[keyCount];

        for (int i = 1; i <= keyCount; i++) {
            keyColumns[i - 1] = columnSet.getColumnView("k" + i);
        }

        for (int row = 0; row < columnSet.getNumRows(); row++) {
            writer.writeItems("field_" + field.getId(), columnSet.getNumRows(), id, keyColumns);
        }


    }


}
