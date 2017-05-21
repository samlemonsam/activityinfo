package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.tree.TreeSelectionModel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.analysis.viewModel.FormForest;

public class FieldTreeView implements IsWidget {


    private final Observable<FormForest> formSet;
    private final VerticalLayoutContainer container;
    private final TreeStore<MeasureTreeNode> treeStore;
    private final Tree<MeasureTreeNode, String> tree;
    private final TextButton calculateButton;

    public FieldTreeView(Observable<FormForest> formSet) {
        this.formSet = formSet;

        FieldFilterField filterField = new FieldFilterField();
        calculateButton = new TextButton("Calculate...");

        ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Search: "));
        toolBar.add(filterField);
        toolBar.add(new FillToolItem());
        toolBar.add(calculateButton);

        this.treeStore = new TreeStore<>(MeasureTreeNode::getId);
        filterField.bind(treeStore);

        this.tree = new Tree<>(treeStore, new ValueProvider<MeasureTreeNode, String>() {
            @Override
            public String getValue(MeasureTreeNode object) {
                return object.getLabel();
            }

            @Override
            public void setValue(MeasureTreeNode object, String value) {

            }

            @Override
            public String getPath() {
                return "element";
            }
        });
        tree.setIconProvider(MeasureTreeNode::getIcon);

        container = new VerticalLayoutContainer();
        container.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(tree, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        formSet.subscribe(this::onFormSetChanged);
    }

    public void setBorders(boolean show) {
        tree.setBorders(show);
    }

    public TextButton getCalculateButton() {
        return calculateButton;
    }

    public TreeSelectionModel<MeasureTreeNode> getSelectionModel() {
        return tree.getSelectionModel();
    }

    private void onFormSetChanged(Observable<FormForest> formSet) {
        treeStore.clear();
        if(formSet.isLoaded()) {
            if(!formSet.get().isEmpty()) {
                fillStore(formSet.get().getFirstTree());
            }
        }
    }

    /**
     * Fills the tree store with fields that can be selected from our FormTree.
     *
     * <p>To keep the presentation </p>
     *
     * @param tree
     */
    private void fillStore(FormTree tree) {
        FieldTreeBuilder builder = new FieldTreeBuilder(treeStore);
        builder.build(tree);
    }



    @Override
    public Widget asWidget() {
        return container;
    }


}
