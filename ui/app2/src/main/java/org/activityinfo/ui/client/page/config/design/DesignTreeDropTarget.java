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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import org.activityinfo.legacy.shared.model.ActivityDTO;
import org.activityinfo.legacy.shared.model.FolderDTO;

import java.util.List;

/**
 * Provides feedback on which drop targets are permitted.
 *
 * For the most part, we only allow inserts. But exceptionally, forms can be moved into folders.
 */
class DesignTreeDropTarget extends TreePanelDropTarget {

    public DesignTreeDropTarget(TreePanel tree) {
        super(tree);
        setAllowSelfAsSource(true);
        setAllowDropOnLeaf(true);
        setFeedback(DND.Feedback.BOTH);
        setAutoExpand(false);
    }

    @Override
    protected void showFeedback(DNDEvent event) {
        final TreePanel.TreeNode overItem = tree.findNode(event.getTarget());
        if (overItem == null) {
            clearStyles(event);
        }

        ModelData sourceModel = tree.getSelectionModel().getSelectedItem();
        ModelData overModel = null;

        if (overItem != null) {
            overModel = overItem.getModel();

            // Dropping a node onto itself? Abort.
            if (overModel == sourceModel) {
                clearStyles(event);
                return;
            }

            // Dropping a node into one of its children? Nope.
            List<ModelData> children = tree.getStore().getChildren(sourceModel, true);
            if (children.contains(overItem.getModel())) {
                clearStyles(event);
                return;
            }
        }

        // Now decide whether this move is legal
        // insert = insert after the overItem as a sibling
        // append = append to the overItem's children
        boolean insert = false;
        boolean append = false;


        // For most items, we will only allow inserting within the same parent
        if (overModel != null &&
            tree.getStore().getParent(sourceModel) == tree.getStore().getParent(overModel)) {

            insert = true;
        }

        // Activities/Forms can be moved into other folders
        if(sourceModel instanceof ActivityDTO) {
            if(overModel == null || overModel instanceof FolderDTO) {
                insert = true;
                append = true;
            }
            if(overModel instanceof ActivityDTO) {
                insert = true;
            }
        }

        // Update the feedback member, which is used
        // by other methods in the superclass to determine
        // which operation is allowed.
        if(append) {
            setFeedback(DND.Feedback.BOTH);
        } else {
            setFeedback(DND.Feedback.INSERT);
        }

        if (append && overItem == null) {
            handleAppend(event, overItem);
        } else if (insert) {
            handleInsert(event, overItem);
        } else if (append) {
            handleAppend(event, overItem);
        } else {
            if (activeItem != null) {
                tree.getView().onDropChange(activeItem, false);
            }
            status = -1;
            activeItem = null;
            appendItem = null;
            Insert.get().hide();
            event.getStatus().setStatus(false);
        }
    }

}
