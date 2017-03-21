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
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.Aggregation;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.view.MeasureDialog;

import java.util.logging.Logger;


public class MeasurePane implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(MeasurePane.class.getName());

    private ContentPanel contentPanel;
    private MeasureDialog dialog;

    private ListStore<MeasureListItem> store;
    private ListView<MeasureListItem, MeasureListItem> list;
    private AnalysisViewModel viewModel;


    public MeasurePane(final AnalysisViewModel viewModel) {
        this.viewModel = viewModel;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addMeasureClicked);

        store = new MeasureListItemStore(viewModel);
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
            dialog = new MeasureDialog(viewModel.getFormStore());
            dialog.addSelectionHandler(this::measureAdded);
        }
        dialog.show();
    }

    private void measureAdded(SelectionEvent<MeasureModel> measure) {
        viewModel.addMeasure(measure.getSelectedItem());
    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }


    private void showMenu(Element element, MeasureListItem item) {

        MeasureModel measure = item.getModel();

        Menu contextMenu = new Menu();

        // Edit the alias
        MenuItem editLabel = new MenuItem();
        editLabel.setText("Edit Label...");
        editLabel.addSelectionHandler(event -> editLabel(measure));
        contextMenu.add(editLabel);

        // Edit the formula...
        MenuItem editFormula = new MenuItem();
        editFormula.setText("Edit Formula...");
        editFormula.addSelectionHandler(event -> editFormula(measure));
        contextMenu.add(editFormula);

        contextMenu.add(new SeparatorMenuItem());

        // Choose the aggregation
        for (Aggregation aggregation : Aggregation.values()) {
            CheckMenuItem aggregationItem = new CheckMenuItem(aggregation.getLabel());
            aggregationItem.setChecked(measure.getAggregation() == aggregation);
            aggregationItem.addCheckChangeHandler(event -> updateAggregation(measure, aggregation));
            contextMenu.add(aggregationItem);
        }
        contextMenu.add(new SeparatorMenuItem());


        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> removeMeasure(measure.getId()));
        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }


    private void removeMeasure(String id) {
        viewModel.updateModel(
            viewModel.getModel().withoutMeasure(id));
    }

    private void editLabel(MeasureModel measure) {
        PromptMessageBox messageBox = new PromptMessageBox("Update measure's alias:", "Enter the new alias");
        messageBox.getTextField().setText(measure.getLabel());

        messageBox.addDialogHideHandler(event -> {
            if(event.getHideButton() == Dialog.PredefinedButton.OK) {
                updateMeasureLabel(measure, messageBox.getValue());
            }
        });

        messageBox.show();
    }

    private void editFormula(MeasureModel measure) {
        FormulaDialog dialog = new FormulaDialog(viewModel.getFormStore(), measure.getFormId());
        dialog.show(measure.getFormula(), formula -> {
            updateMeasureFormula(measure, formula.getFormula());

        });
    }

    private void updateMeasureLabel(MeasureModel measureModel, String newLabel) {
        ImmutableMeasureModel updatedMeasure = ImmutableMeasureModel.builder()
                .from(measureModel)
                .label(newLabel)
                .build();

        viewModel.updateModel(
                viewModel.getModel().withMeasure(updatedMeasure));
    }


    private void updateMeasureFormula(MeasureModel measure, String formula) {
        ImmutableMeasureModel updatedMeasure = ImmutableMeasureModel.builder()
                .from(measure)
                .formula(formula)
                .build();

        viewModel.updateModel(
                viewModel.getModel().withMeasure(updatedMeasure));
    }


    private void updateAggregation(MeasureModel measureModel, Aggregation aggregation) {
        ImmutableMeasureModel updatedMeasure = ImmutableMeasureModel.builder()
                .from(measureModel)
                .aggregation(aggregation)
                .build();

        viewModel.updateModel(
                viewModel.getModel().withMeasure(updatedMeasure));
    }
}
