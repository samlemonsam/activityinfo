package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableList;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.view.MeasureDialog;

import java.util.List;


public class MeasurePane implements IsWidget {

    private ContentPanel contentPanel;
    private MeasureDialog dialog;

    private ListStore<MeasureListItem> store;
    private ListView<MeasureListItem, MeasureListItem> list;
    private AnalysisViewModel model;


    public MeasurePane(final AnalysisViewModel model) {
        this.model = model;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addMeasureClicked);

        store = new ListStore<>(MeasureListItem::getId);
        ObservableList<Observable<MeasureListItem>> map = model.getMeasures().map(MeasureListItem::compute);
        Observable<List<MeasureListItem>> measures = Observable.flatten(map);
        measures.subscribe(new Observer<List<MeasureListItem>>() {
            @Override
            public void onChange(Observable<List<MeasureListItem>> observable) {
                if(observable.isLoading()) {
                    store.clear();
                } else {
                    store.replaceAll(measures.get());
                }
            }
        });


        store = new ListStore<>(MeasureListItem::getId);
        list = new ListView<>(store,
                new IdentityValueProvider<>(),
                new PillCell<>(MeasureListItem::getLabel, this::showMenu));
        this.contentPanel = new ContentPanel();
        this.contentPanel.setHeading(I18N.CONSTANTS.measures());
        this.contentPanel.addTool(addButton);
        this.contentPanel.setWidget(list);
    }



    private void addMeasureClicked(SelectEvent event) {
        if (dialog == null) {
            dialog = new MeasureDialog(model.getFormStore());
            dialog.addSelectionHandler(this::measureAdded);
        }
        dialog.show();
    }

    private void measureAdded(SelectionEvent<MeasureModel> measure) {
        model.addMeasure(measure.getSelectedItem());
    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }


    private void showMenu(Element element, MeasureListItem item) {

        MeasureModel measure = item.getModel();

        Menu contextMenu = new Menu();

        // Edit the alias
        MenuItem editAlias = new MenuItem();
        editAlias.setText("Edit Alias...");
        editAlias.addSelectionHandler(event -> editAlias(measure));
        contextMenu.add(editAlias);

        // Edit the formula...
        MenuItem editFormula = new MenuItem();
        editFormula.setText("Edit Formula...");
        editFormula.addSelectionHandler(event -> editFormula(measure));
        contextMenu.add(editFormula);

        contextMenu.add(new SeparatorMenuItem());

        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> model.removeMeasure(measure.getId()));
        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }

    private void editAlias(MeasureModel measure) {
        PromptMessageBox messageBox = new PromptMessageBox("Update measure's alias:", "Enter the new alias");
        messageBox.getTextField().setText(measure.getLabel());

        messageBox.addDialogHideHandler(event -> {
            if(event.getHideButton() == Dialog.PredefinedButton.OK) {
                model.updateMeasureLabel(measure.getId(), messageBox.getValue());
            }
        });

        messageBox.show();
    }

    private void editFormula(MeasureModel measure) {
        FormulaDialog dialog = new FormulaDialog(model.getFormStore(), measure.getFormId());
        dialog.show(measure.getFormula(), formula -> {
            model.updateMeasureFormula(measure.getId(), formula.getFormula());

        });
    }
}
