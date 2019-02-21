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
package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.info.InfoConfig;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.promise.Promise;
import org.activityinfo.model.analysis.pivot.Axis;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.analysis.pivot.viewModel.WorkingModel;
import org.activityinfo.ui.client.chrome.HasTitle;

public class AnalysisView implements IsWidget, HasTitle {

    private BorderLayoutContainer container;
    private AnalysisViewModel model;
    private PivotTableView pivotTableView;
    private SubscriptionSet subscriptions = new SubscriptionSet();

    public AnalysisView(AnalysisViewModel model) {
        this.model = model;
        container = new BorderLayoutContainer();
        container.addAttachHandler(this::onAttachEvent);
        createPanes();

        AnalysisBundle.INSTANCE.getStyles().ensureInjected();
    }

    private void onAttachEvent(AttachEvent attachEvent) {
        if(attachEvent.isAttached()) {
            subscriptions.add(this.model.getWorking().subscribe(this::onWorkingModelChanged));
        } else {
            subscriptions.unsubscribeAll();
        }

    }

    private void onWorkingModelChanged(Observable<WorkingModel<PivotModel>> workingModel) {
        if(workingModel.isLoading()) {
            this.container.mask();
        } else {
            this.container.unmask();
        }
    }


    private void createPanes() {

        MeasurePane measurePane = new MeasurePane(model);
        DimensionPane rowPane = new DimensionPane(model, Axis.ROW);
        DimensionPane columnPane = new DimensionPane(model, Axis.COLUMN);
        pivotTableView = new PivotTableView(model);

        VerticalLayoutContainer pane = new VerticalLayoutContainer();
        pane.add(measurePane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.4));
        pane.add(rowPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));
        pane.add(columnPane, new VerticalLayoutContainer.VerticalLayoutData(1, 0.3));

        BorderLayoutContainer.BorderLayoutData paneLayout = new BorderLayoutContainer.BorderLayoutData();
        paneLayout.setSize(0.3); // 30% of view
        paneLayout.setMaxSize(400);
        container.setEastWidget(pane, paneLayout);

        container.setCenterWidget(pivotTableView);
    }

    @Override
    public Widget asWidget() {
        return container;
    }


    private void save(SelectEvent event) {
        ensureTitle().join(this::ensureFolder).join(this::executeSave);

    }

    private Promise<Void> ensureTitle() {
        if(model.getWorking().isLoading()) {
            return new Promise<>();
        }
        if(model.getWorking().get().getLabel().isPresent()) {
            return Promise.done();
        }
        Promise<Void> result = new Promise<>();
        PromptMessageBox box = new PromptMessageBox(I18N.CONSTANTS.save(), I18N.CONSTANTS.chooseReportTitle());
        box.show();
        box.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.OK) {
                    AnalysisView.this.model.updateTitle(box.getTextField().getValue());
                    result.resolve(null);
                }
            }
        });
        return result;
    }

    private Promise<Void> ensureFolder(Void input) {

        if(model.getWorking().get().getParentId().isPresent()) {
            return Promise.done();
        }

        Promise<Void> result = new Promise<>();

        FolderDialog dialog = new FolderDialog(model.getFormStore());
        dialog.show();
        dialog.getOkButton().addSelectHandler(event -> {
            dialog.hide();
            AnalysisView.this.model.updateFolderId(dialog.getSelected().getId());
            result.onSuccess(null);
        });
        return result;
    }

    private Promise<Void> executeSave(Void input) {
        Promise<Void> result = AnalysisView.this.model.getFormStore().updateAnalysis(model.getWorking().get().buildUpdate());
        result.then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Void result) {
                DefaultInfoConfig config = new DefaultInfoConfig(I18N.CONSTANTS.saved(), I18N.CONSTANTS.savedChanges());
                config.setPosition(InfoConfig.InfoPosition.BOTTOM_RIGHT);
                config.setDisplay(1000);
                Info.display(config);
            }
        });
        return result;
    }

    @Override
    public Observable<String> getTitle() {
        return model.getWorking().transform(working -> working.getLabel().or(I18N.CONSTANTS.untitledReport()));
    }
}
