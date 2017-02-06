package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.measureDialog.model.FormSet;

public class FieldTreeView implements IsWidget {


    private final Observable<FormSet> formSet;
    private final VerticalLayoutContainer container;
    private TreeStore<FieldElement> treeStore;
    private final Tree<FieldElement, String> tree;
    private final TextButton calculateButton;

    public FieldTreeView(Observable<FormSet> formSet) {
        this.formSet = formSet;

        FieldFilterField filterField = new FieldFilterField();
        calculateButton = new TextButton("Calculate...");

        ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Search: "));
        toolBar.add(filterField);
        toolBar.add(new FillToolItem());
        toolBar.add(calculateButton);

        this.treeStore = new TreeStore<>(FieldElement::getId);
        filterField.bind(treeStore);

        this.tree = new Tree<>(treeStore, new ValueProvider<FieldElement, String>() {
            @Override
            public String getValue(FieldElement object) {
                return object.getLabel();
            }

            @Override
            public void setValue(FieldElement object, String value) {

            }

            @Override
            public String getPath() {
                return "element";
            }
        });
        tree.setIconProvider(FieldElement::getIcon);
        tree.setCheckable(true);
        tree.setCheckStyle(Tree.CheckCascade.TRI);

        container = new VerticalLayoutContainer();
        container.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(tree, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        formSet.subscribe(this::onFormSetChanged);
    }

    public TextButton getCalculateButton() {
        return calculateButton;
    }

    private void onFormSetChanged(Observable<FormSet> formSet) {
        treeStore.clear();
        if(formSet.isLoaded()) {
            if(!formSet.get().isEmpty()) {
                treeStore.add(FieldElement.count());

                for (FormField field : formSet.get().getCommonFields()) {
                    if (field.getType() instanceof QuantityType || field.getType() instanceof CalculatedFieldType) {
                        treeStore.add(FieldElement.forField(field));
                    }
                }
            }
        }
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
