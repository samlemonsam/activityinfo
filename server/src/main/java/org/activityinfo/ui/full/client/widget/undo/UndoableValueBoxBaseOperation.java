package org.activityinfo.ui.full.client.widget.undo;
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

import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * @author yuriyz on 2/17/14.
 */
public class UndoableValueBoxBaseOperation implements IsUndoable {

    private final ValueBoxBase valueBoxBase;
    private final Object value;
    private Object newValue;

    public UndoableValueBoxBaseOperation(ValueBoxBase valueBoxBase, Object value) {
        this.valueBoxBase = valueBoxBase;
        this.value = value;
    }

    @Override
    public void undo() {
        newValue = valueBoxBase.getValue();
        valueBoxBase.setValue(value, false);
    }

    @Override
    public void redo() {
        valueBoxBase.setValue(newValue, false);
    }

    public Object getValue() {
        return value;
    }

    public ValueBoxBase getValueBoxBase() {
        return valueBoxBase;
    }
}
