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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormModel;
import org.activityinfo.ui.client.component.form.PanelFiller;
import org.activityinfo.ui.client.component.form.RelevanceHandler;
import org.activityinfo.ui.client.widget.LoadingPanel;

import java.util.List;

/**
 * @author yuriyz on 02/17/2015.
 */
public class PeriodSubFormPanel implements SubFormPanel {

    private final FlowPanel panel;
    private final FormClass subForm;
    private final RelevanceHandler relevanceHandler;
    private final FormModel formModel;
    private final PanelFiller panelFiller;
    private final int depth;
    private final LoadingPanel<Void> loadingPanel;
    private PeriodTabStrip tabStrip;

    public PeriodSubFormPanel(FormModel formModel, FormClass subForm, RelevanceHandler relevanceHandler, PanelFiller panelFiller, int depth) {
        this.subForm = subForm;
        this.formModel = formModel;
        this.relevanceHandler = relevanceHandler;
        this.panelFiller = panelFiller;
        this.depth = depth;

        this.loadingPanel = new LoadingPanel<>();
        this.loadingPanel.setDisplayWidget(this);
        this.loadingPanel.showWithoutLoad();

        this.panel = new FlowPanel();
    }

    private void onInstanceLoaded() {
        tabStrip = createTabStrip();
        panel.add(tabStrip);
        panelFiller.add(subForm, depth, panel);
    }

    private PeriodTabStrip createTabStrip() {
        PeriodTabStrip tabStrip = new PeriodTabStrip(subForm.getSubFormKind());
        tabStrip.addValueChangeHandler(new ValueChangeHandler<Tab>() {
            @Override
            public void onValueChange(ValueChangeEvent<Tab> event) {
                onTabChange();
            }
        });

        String key = formModel.getStateProvider().getString("subform.kind." + subForm.getSubFormKind().name());
        if (!Strings.isNullOrEmpty(key)) {
            tabStrip.setValue(key, false);
        }

        return tabStrip;
    }

    private void onTabChange() {
        Tab tab = tabStrip.getValue();

        Optional<FormInstance> instance = formModel.getSubformValueInstance(subForm, formModel.getWorkingRootInstance(), ResourceId.valueOf(tab.getId()));
        if (instance.isPresent()) {
            formModel.applyInstanceValues(instance.get(), subForm);
        }

        formModel.getStateProvider().set("subform.kind." + tab.getKind().name(), tab.getId());
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public LoadingPanel<Void> getLoadingPanel() {
        return loadingPanel;
    }

    @Override
    public Promise<Void> show(Void value) {
        return new SubFormInstanceLoader(formModel).load(subForm).then(new Function<List<FormInstance>, Void>() {
            @Override
            public Void apply(List<FormInstance> input) {
                onInstanceLoaded();
                return null;
            }
        });
    }

    public Tab getSelectedTab() {
        return tabStrip.getValue();
    }
}
