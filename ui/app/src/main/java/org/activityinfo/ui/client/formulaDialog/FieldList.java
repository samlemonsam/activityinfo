package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.observable.Observable;

/**
 * Created by alex on 3-2-17.
 */
public class FieldList implements IsWidget {

    private TreeStore<FormulaElement> store;
    private Tree<FormulaElement, FormulaElement> tree;
    private final FormulaElement fieldFolder;

    public FieldList(Observable<FormTree> formTree) {
        store = new TreeStore<>(FormulaElement.KEY_PROVIDER);
        tree = new Tree<>(store, FormulaElement.VALUE_PROVIDER);
        tree.setCell(new FormulaElementCell());
        tree.setIconProvider(element -> element.getIcon());
        tree.setBorders(true);

        fieldFolder = FormulaElement.folder("_fields", I18N.CONSTANTS.fields());
        store.add(fieldFolder);
        tree.setLeaf(fieldFolder, false);
        tree.setExpanded(fieldFolder, true);

        formTree.subscribe(this::onTreeUpdated);
    }


    @Override
    public Widget asWidget() {
        return tree;
    }

    private void onTreeUpdated(Observable<FormTree> formTree) {
        store.removeChildren(fieldFolder);
        if(formTree.isLoaded()) {
            for (FormTree.Node node : formTree.get().getRootFields()) {
                addNode(fieldFolder, node);
            }
        }
    }

    private void addNode(FormulaElement parentNode, FormTree.Node node) {
        FormulaElement field = FormulaElement.fieldNode(node);
        store.add(parentNode, field);
        for (FormTree.Node childNode : node.getChildren()) {
            addNode(field, childNode);
        }
    }

}
