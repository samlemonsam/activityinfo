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

import com.sencha.gxt.dnd.core.client.DndDragEnterEvent;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DropTarget;
import org.activityinfo.ui.codemirror.client.CodeMirror;
import org.activityinfo.ui.codemirror.client.LeftTop;
import org.activityinfo.ui.codemirror.client.Pos;
import org.activityinfo.ui.codemirror.client.Token;

import java.util.logging.Logger;

public class FormulaDropTarget extends DropTarget {

    private final Logger LOGGER = Logger.getLogger(FormulaDropTarget.class.getName());

    private final FormulaEditor editor;

    private Pos insertPos;

    private int currentLine = -1;
    private int lineLength = -1;
    private Token[] lineTokens = new Token[0];
    private int lineGeneration;


    public FormulaDropTarget(FormulaEditor dropWidget) {
        super(dropWidget.asWidget());
        this.editor = dropWidget;
    }

    @Override
    protected void showFeedback(DndDragMoveEvent event) {
        super.showFeedback(event);
    }

    @Override
    protected void onDragEnter(DndDragEnterEvent event) {
        super.onDragEnter(event);

        // Focus the editor so the cursor shows the insert target
        editor.getEditor().focus();

        // Check to see if we need to clear our cache of the line's tokens
        if(currentLine != -1) {
            if(!editor.getEditor().getDoc().isClean(lineGeneration)) {
                clearLineCache();
            }
        }
    }

    private void clearLineCache() {
        currentLine = -1;
    }

    @Override
    protected void onDragMove(DndDragMoveEvent event) {
        super.onDragMove(event);
        LeftTop leftTop = LeftTop.create(event.getDragMoveEvent().getX(), event.getDragMoveEvent().getY());

        updateInsertPos(leftTop);
    }

    /**
     * Updates the insertion position to something sensible.
     *
     * @param leftTop the current position position
     */
    private void updateInsertPos(LeftTop leftTop) {

        Pos mousePos = editor.getEditor().coordsChar(leftTop);

        // Re-use parse information if possible for this line if possible
        if(mousePos.line != currentLine) {
            currentLine = mousePos.line;
            lineTokens = editor.getEditor().getLineTokens(currentLine, true);
            if(lineTokens == null || lineTokens.length == 0) {
                lineLength = 0;
            } else {
                lineLength = lineTokens[lineTokens.length - 1].getEnd();
            }
            lineGeneration = editor.getEditor().getDoc().changeGeneration();
        }

        // Find an insert position on a token boundary
        this.insertPos = CodeMirror.create(currentLine, findInsertPos(mousePos.ch));

        // Update cursor to display the insertion point
        editor.getEditor().getDoc().setCursor(insertPos);

    }

    /**
     *
     * Finds a sensible insertion point. We don't want to insert things in the middle of
     * field names, etc.
     *
     * @param mouseCh The character index over which the mouse is positioned.
     * @return the closest character index of a token boundary
     */
    private int findInsertPos(int mouseCh) {

        if(mouseCh >= lineLength) {
            return lineLength;
        }

        for (int i = 0; i < lineTokens.length; i++) {
            Token token = lineTokens[i];
            if(mouseCh <= token.getEnd()) {
                int fromStart = (mouseCh - token.getStart());
                int fromEnd = (token.getEnd() - mouseCh);
                if(fromStart < fromEnd) {
                    return token.getStart();
                } else {
                    return token.getEnd();
                }
            }
        }
        return lineLength;
    }


    @Override
    protected void onDragDrop(DndDropEvent event) {

        if(event.getData() instanceof FormulaElement) {
            FormulaElement element = (FormulaElement) event.getData();

            String expression = element.getExpr().asExpression();
            editor.insertAt(expression, insertPos);
        }
    }
}
