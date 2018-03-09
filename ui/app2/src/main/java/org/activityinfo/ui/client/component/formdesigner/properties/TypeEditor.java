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
package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.user.client.ui.IsWidget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

public abstract class TypeEditor<T extends FieldType> implements IsWidget {

    private FieldWidgetContainer currentField;

    @SuppressWarnings("unchecked")
    public final void show(FieldWidgetContainer container) {
        if(accept(container.getFormField().getType())) {
            this.currentField = container;
            show((T)container.getFormField().getType());
            asWidget().setVisible(true);

        } else {
            asWidget().setVisible(false);
        }
    }

    protected abstract boolean accept(FieldType type);

    @SuppressWarnings("unchecked")
    protected final T currentType() {
        return (T)currentField.getFormField().getType();
    }

    protected abstract void show(T type);

    protected final void updateType(T updatedType) {
        currentField.getFormField().setType(updatedType);
        currentField.syncWithModel();
    }
}
