package org.activityinfo.ui.client.analysis.view;

import com.google.common.base.Function;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.analysis.AnalysisUpdate;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.Axis;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.EffectiveModel;
import org.activityinfo.ui.client.chrome.HasTitle;

import javax.annotation.Nullable;

public class AnalysisView implements IsWidget, HasTitle {

    private BorderLayoutContainer container;
    private AnalysisViewModel model;
    private PivotTableView pivotTableView;

    public AnalysisView(AnalysisViewModel model) {
        this.model = model;
        this.model = model;
        container = new BorderLayoutContainer();
        createPanes();

        AnalysisBundle.INSTANCE.getStyles().ensureInjected();

        pivotTableView.getSaveButton().addSelectHandler(this::save);
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

        AnalysisModel model = this.model.getEffectiveModel().get().getModel();

        ensureTitle(model).join(this::ensureFolder).join(this::executeSave);

    }

    private Promise<AnalysisModel> ensureTitle(AnalysisModel model) {
        if(model.getTitle().isPresent()) {
            return Promise.resolved(model);
        }
        Promise<AnalysisModel> result = new Promise<>();
        PromptMessageBox box = new PromptMessageBox(I18N.CONSTANTS.save(), I18N.CONSTANTS.chooseReportTitle());
        box.show();
        box.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                if (event.getHideButton() == Dialog.PredefinedButton.OK) {
                    result.resolve(AnalysisView.this.model.updateTitle(box.getTextField().getValue()));
                }
            }
        });
        return result;
    }

    private Promise<AnalysisModel> ensureFolder(AnalysisModel input) {

        if(input.getFolderId().isPresent()) {
            return Promise.resolved(input);
        }

        Promise<AnalysisModel> result = new Promise<>();

        FolderDialog dialog = new FolderDialog(model.getFormStore());
        dialog.show();
        dialog.getOkButton().addSelectHandler(new SelectEvent.SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                dialog.hide();
                result.onSuccess(AnalysisView.this.model.updateFolderId(dialog.getSelected().getId()));
            }
        });
        return result;
    }

    private Promise<Void> executeSave(AnalysisModel input) {

        AnalysisUpdate update = new AnalysisUpdate();
        update.setId(input.getId());
        update.setFolderId(input.getFolderId().get());
        update.setType("pivot");
        update.setModel(input.toJson());

        return AnalysisView.this.model.getFormStore().updateAnalysis(update);
    }

    @Override
    public Observable<String> getTitle() {
        return model.getEffectiveModel().transform(new Function<EffectiveModel, String>() {
            @Nullable
            @Override
            public String apply(@Nullable EffectiveModel effectiveModel) {
                return effectiveModel.getModel().getTitle().or(I18N.CONSTANTS.untitledReport());
            }
        });
    }
}
