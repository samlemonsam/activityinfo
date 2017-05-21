package org.activityinfo.ui.client.measureDialog.view;

import com.google.common.base.Strings;
import com.sencha.gxt.data.shared.TreeStore;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.store.query.server.FormSourceSyncImpl;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.testing.ReferralSubForm;
import org.activityinfo.store.testing.TestingCatalog;
import org.activityinfo.ui.client.store.TestingFormStore;
import org.junit.Test;

import static org.junit.Assert.*;

public class FieldTreeBuilderTest {

    @Test
    public void subForms() {

        FormSource formStore = new FormSourceSyncImpl(new TestingCatalog(), 1);
        FormTree tree = formStore.getFormTree(ReferralSubForm.FORM_ID).waitFor();

        TreeStore<MeasureTreeNode> treeStore = new TreeStore<>(MeasureTreeNode::getId);
        FieldTreeBuilder builder = new FieldTreeBuilder(treeStore);

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