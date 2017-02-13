package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DragSource;
import com.sencha.gxt.widget.core.client.tree.Tree;


public class FieldTreeSource extends DragSource {


    public FieldTreeSource(Tree<FormulaElement, ?> tree) {
        super(tree);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tree<FormulaElement, ?> getWidget() {
        return (Tree<FormulaElement, ?>) super.getWidget();
    }



    @Override
    protected void onDragDrop(DndDropEvent event) {
        // NO-OP for us.
    }

    @Override
    protected void onDragStart(DndDragStartEvent event) {
        Element startTarget = event.getDragStartEvent().getStartElement().cast();
        Tree.TreeNode<FormulaElement> start = getWidget().findNode(startTarget);
        if (start == null || !getWidget().getView().isSelectableTarget(start.getModel(), startTarget)) {
            event.setCancelled(true);
            return;
        }

        FormulaElement element = start.getModel();

        event.setData(element);
        event.setCancelled(false);
        event.getStatusProxy().update(SafeHtmlUtils.fromString(element.getExpr().asExpression()));
    }
}
