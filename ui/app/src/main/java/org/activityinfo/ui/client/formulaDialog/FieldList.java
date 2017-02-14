package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.logging.Logger;


public class FieldList implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(FieldList.class.getName());

    private VerticalLayoutContainer container;
    private FormulaElementFilter filter;
    private TreeStore<FormulaElement> store;
    private Tree<FormulaElement, FormulaElement> tree;
    private final Subscription subscription;

    public FieldList(Observable<FormTree> formTree) {
        store = new TreeStore<>(FormulaElement.KEY_PROVIDER);

        filter = new FormulaElementFilter();
        filter.bind(store);

        tree = new Tree<FormulaElement, FormulaElement>(store, FormulaElement.VALUE_PROVIDER) {

            @Override
            protected void onDoubleClick(Event event) {
                super.onDoubleClick(event);

                TreeNode<FormulaElement> node = findNode(event.getEventTarget().<Element> cast());
                if(isLeaf(node.getModel())) {
                    onDoubleClicked(node.getModel());
                }
            }
        };
        tree.setCell(new FormulaElementCell());
        tree.setIconProvider(element -> element.getIcon());
        tree.setBorders(true);

        container = new VerticalLayoutContainer();
        container.add(filter, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        container.add(tree, new VerticalLayoutContainer.VerticalLayoutData(1, 1));

        // Start listening for changes to the form
        subscription = formTree.subscribe(this::onTreeUpdated);

        // Initialize Drag and Drop
        new FieldTreeSource(tree);
    }

    @Override
    public Widget asWidget() {
        return container;
    }

    private void onTreeUpdated(Observable<FormTree> formTree) {
        store.clear();
        if(formTree.isLoaded()) {
            for (FormTree.Node node : formTree.get().getRootFields()) {
                addNode(null, node);
            }
        }
    }

    private void addNode(FormulaElement parentNode, FormTree.Node node) {
        FormulaElement field = FormulaElement.fieldNode(node);
        if(parentNode == null) {
            store.add(field);
        } else {
            store.add(parentNode, field);
        }
        for (FormTree.Node childNode : node.getChildren()) {
            addNode(field, childNode);
        }
        if(node.getType() instanceof EnumType) {
            EnumType enumType = (EnumType) node.getType();
            for (EnumItem enumItem : enumType.getValues()) {
                store.add(field, new FormulaElement(field, enumItem));
            }
        }
    }

    public void disconnect() {
        subscription.unsubscribe();
    }


    private void onDoubleClicked(FormulaElement model) {
        LOGGER.info("Double clicked: " + model.getCode());
    }
}
