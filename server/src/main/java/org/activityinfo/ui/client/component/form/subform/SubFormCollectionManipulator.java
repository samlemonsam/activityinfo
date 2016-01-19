package org.activityinfo.ui.client.component.form.subform;
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

import com.google.gwt.user.client.ui.FlowPanel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.component.form.FormModel;

/**
 * @author yuriyz on 01/18/2016.
 */
public class SubFormCollectionManipulator {

    private final FormClass subForm;
    private final FormModel formModel;

    public SubFormCollectionManipulator(FormClass subForm, FormModel formModel) {
        this.subForm = subForm;
        this.formModel = formModel;
    }

    public void show(FlowPanel panel) {

    }
}
