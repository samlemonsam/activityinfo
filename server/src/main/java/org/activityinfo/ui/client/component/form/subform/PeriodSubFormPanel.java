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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.RelevanceHandler;

import javax.annotation.Nullable;

/**
 * @author yuriyz on 02/17/2015.
 */
public class PeriodSubFormPanel implements IsWidget {

    private final FlowPanel panel;
    private final FormClass subForm;
    private final PeriodTabStrip tabStrip;
    private final RelevanceHandler relevanceHandler;

    @Nullable
    private PeriodValue periodValue = null; // if not null then period instance generator is in use

    private FormModel formModel;

    public PeriodSubFormPanel(FormModel formModel, FormClass subForm, RelevanceHandler relevanceHandler) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.relevanceHandler = relevanceHandler;

        this.panel = new FlowPanel();
        this.tabStrip = new PeriodTabStrip(subForm.getSubFormKind());
        this.panel.add(tabStrip);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public FlowPanel getPanel() {
        return panel;
    }
}
