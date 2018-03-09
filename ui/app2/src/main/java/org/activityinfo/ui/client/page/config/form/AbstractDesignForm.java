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
package org.activityinfo.ui.client.page.config.form;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BindingEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

/**
 * Base class for design forms
 *
 * @author Alex Bertram
 */
public abstract class AbstractDesignForm extends FormPanel {


    @Override
    public void setReadOnly(boolean readOnly) {
        for (Field<?> f : getFields()) {
            f.setEnabled(!readOnly);
            f.setReadOnly(readOnly);
        }
    }

    public abstract FormBinding getBinding();

    public int getPreferredDialogWidth() {
        return 450;
    }

    public int getPreferredDialogHeight() {
        return 420;
    }

    public void hideFieldWhenNull(final Field<?> field) {
        getBinding().addListener(Events.Bind, new Listener<BindingEvent>() {

            @Override
            public void handleEvent(BindingEvent be) {
                field.setVisible(field.getValue() != null);
                field.setReadOnly(true);
            }
        });
    }
}