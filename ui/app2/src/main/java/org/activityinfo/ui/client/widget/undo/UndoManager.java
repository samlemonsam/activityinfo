package org.activityinfo.ui.client.widget.undo;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author yuriyz on 2/17/14.
 */
public class UndoManager {

    private final List<IsUndoable> undoables = Lists.newArrayList();
    private final List<UndoListener> listeners = Lists.newArrayList();
    private int position = -1;

    public UndoManager() {
    }

    public void addUndoable(IsUndoable undoable) {
        if (undoable != null) {
            position++;
            undoables.add(position, undoable);
            removeUndoablesFrom(position);
            fireCreatedEvent(new UndoableCreatedEvent(undoable));
        }
    }

    /**
     * Removes undoables from list start from position but excluding this position itself.
     *
     * @param position position from which undoables will be removed but excluding this position itself
     */
    private void removeUndoablesFrom(int position) {
        for (int i = position + 1; i < undoables.size(); i++) {
            undoables.remove(i);
        }
    }

    private IsUndoable getUndoAtPosition() {
        return getUndoableAtPosition(position);
    }

    private IsUndoable getRedoAtPosition() {
        return getUndoableAtPosition(position + 1);
    }

    private IsUndoable getUndoableAtPosition(int position) {
        if (position < 0 || position >= undoables.size()) {
            return null;
        }
        return undoables.get(position);
    }

    public void undo() {
        final IsUndoable undoable = getUndoAtPosition();
        if (undoable != null) {
            position--;
            undoable.undo();
            fireExecutedEvent(new UndoableExecutedEvent(undoable));
        }
    }

    public boolean canUndo() {
        return getUndoAtPosition() != null;
    }

    public boolean canRedo() {
        return getRedoAtPosition() != null;
    }

    public void redo() {
        final IsUndoable undoable = getRedoAtPosition();
        if (undoable != null) {
            position++;
            undoable.redo();
            fireExecutedEvent(new UndoableExecutedEvent(undoable));
        }
    }

    private void fireExecutedEvent(UndoableExecutedEvent undoEvent) {
        for (UndoListener listener : listeners) {
            listener.onUndoableExecuted(undoEvent);
        }
    }

    private void fireCreatedEvent(UndoableCreatedEvent undoEvent) {
        for (UndoListener listener : listeners) {
            listener.onUndoableCreated(undoEvent);
        }
    }

    public void addListener(UndoListener listener) {
        listeners.add(listener);
    }

    public void removeListener(UndoListener listener) {
        listeners.remove(listener);
    }
}
