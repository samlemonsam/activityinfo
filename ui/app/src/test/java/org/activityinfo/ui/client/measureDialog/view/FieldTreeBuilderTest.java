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
package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Strings;
import com.sencha.gxt.data.shared.TreeStore;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.store.query.server.FormSourceSyncImpl;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.testing.ReferralSubForm;
import org.activityinfo.store.testing.TestingDatabaseProvider;
import org.activityinfo.store.testing.TestingStorageProvider;
import org.junit.Before;
import org.junit.Test;

public class FieldTreeBuilderTest {

    private TestingStorageProvider storageProvider;
    private TestingDatabaseProvider databaseProvider;

    private final int userId = 1;

    @Before
    public void setup() {
        this.storageProvider = new TestingStorageProvider();
        this.databaseProvider = storageProvider.getDatabaseProvider();
    }

    @Test
    public void subForms() {

        FormSource formStore = new FormSourceSyncImpl(storageProvider, userId);
        FormTree tree = formStore.getFormTree(ReferralSubForm.FORM_ID).waitFor();

        TreeStore<MeasureTreeNode> treeStore = new TreeStore<>(MeasureTreeNode::getId);
        FieldTreeBuilder builder = new FieldTreeBuilder(tree, treeStore);

        try {
            builder.build(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("===== FormTree === ");
        FormTreePrettyPrinter.print(tree);

        System.out.println();

        System.out.println("===== TreeStore === ");
        prettyPrint(treeStore);

    }

    private void prettyPrint(TreeStore<MeasureTreeNode> treeStore) {
        for (MeasureTreeNode node : treeStore.getRootItems()) {
            System.out.println(node.getLabel());
            prettyPrint(treeStore, node, 1);
        }

    }

    private void prettyPrint(TreeStore<MeasureTreeNode> treeStore, MeasureTreeNode parent, int indent) {
        for (MeasureTreeNode node : treeStore.getAllChildren(parent)) {
            System.out.println(Strings.repeat(".", indent * 3) +node.getLabel());
            prettyPrint(treeStore, node, indent + 1);
        }
    }

}