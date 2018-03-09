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
