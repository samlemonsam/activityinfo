package org.activityinfo.ui.client.component.formdesigner.container;
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

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;

import java.util.List;

/**
 * @author yuriyz on 02/04/2015.
 */
public class SubFormTabsPresenter {

    private final SubFormTabs view;
    private final FormDesigner formDesigner;

    public SubFormTabsPresenter(SubFormTabs view, FormDesigner formDesigner) {
        this.view = view;
        this.formDesigner = formDesigner;
    }

    public void set(List<FormInstance> instances) {
        // todo
    }


    public void generate(PeriodValue period) {
        // todo
    }
}
