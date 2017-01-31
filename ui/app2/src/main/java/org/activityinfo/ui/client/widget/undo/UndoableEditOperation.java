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

import com.google.gwt.user.client.ui.HasValue;

/**
 * @author yuriyz on 2/17/14.
 */
public class UndoableEditOperation implements IsUndoable {

    private final HasValue widget;
    private final Object value;
    private Object newValue;

    public UndoableEditOperation(HasValue widget, Object value) {
        this.widget = widget;
        this.value = value;
    }

    @Override
    public void undo() {
        newValue = widget.getValue();
        widget.setValue(value, false);
    }

    @Override
    public void redo() {
        widget.setValue(newValue, false);
    }

    public Object getValue() {
        return value;
    }

    public HasValue getWidget() {
        return widget;
    }
}
