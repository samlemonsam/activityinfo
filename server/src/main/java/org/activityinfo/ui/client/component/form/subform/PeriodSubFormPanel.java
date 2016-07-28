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

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.type.period.PeriodValue;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.RelevanceHandler;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author yuriyz on 02/17/2015.
 */
public class PeriodSubFormPanel implements IsWidget {

    private final FlowPanel panel;
    private FormClass subForm;
    private final PeriodTabStrip tabStrip;
    
    private final ResourceLocator resourceLocator;
    
    private boolean designMode;
    private Optional<RelevanceHandler> relevanceHandler = Optional.absent();

    @Nullable
    private PeriodValue periodValue = null; // if not null then period instance generator is in use

    private FormModel formModel;
    private FormDesigner formDesigner;
    
    private PeriodSubFormPanel(ResourceLocator locator, FormClass subForm) {
        this.panel = new FlowPanel();
        this.subForm = subForm;
        this.tabStrip = new PeriodTabStrip(subForm.getSubFormKind());
        this.panel.add(tabStrip);
        this.resourceLocator = locator;
    }

    public PeriodSubFormPanel(FormModel formModel,
                              FormClass subForm,
                              RelevanceHandler relevanceHandler) {
        this(formModel.getLocator(), subForm);
        this.designMode = false;
        this.relevanceHandler = Optional.of(relevanceHandler);
        this.designMode = false;
    }

    public PeriodSubFormPanel(@Nonnull FormDesigner formDesigner, FormClass subForm) {
        this(formDesigner.getResourceLocator(), subForm);
        this.formDesigner = formDesigner;
        this.designMode = true;
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
