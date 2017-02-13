package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.FieldMeasure;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.view.MeasureDialog;


public class MeasurePane implements IsWidget {

    private ContentPanel contentPanel;
    private MeasureDialog dialog;

    private ObservingListStore<MeasureModel> store;
    private ListView<MeasureModel, MeasureModel> list;
    private AnalysisModel model;


    public MeasurePane(final AnalysisModel model) {
        this.model = model;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addMeasureClicked);

        store = new ObservingListStore<>(model.getMeasures(), MeasureModel::getKey);
        list = new ListView<>(store,
                new IdentityValueProvider<>(),
                new PillCell<>(MeasureModel::getLabel, this::showMenu));
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


    private void showMenu(Element element, MeasureModel measure) {

        Menu contextMenu = new Menu();

        // Edit the formula...
        MenuItem editFormula = new MenuItem();
        editFormula.setText("Edit Formula...");
        editFormula.addSelectionHandler(event -> editFormula(measure));
        editFormula.setEnabled(measure instanceof FieldMeasure);
        contextMenu.add(editFormula);

        contextMenu.add(new SeparatorMenuItem());

        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> model.removeMeasure(measure.getKey()));
        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }


    private void editFormula(MeasureModel measure) {
        FieldMeasure fieldMeasure = (FieldMeasure) measure;
        FormulaDialog dialog = new FormulaDialog(model.getFormStore(), ((FieldMeasure) measure).getFormId());
        dialog.show(fieldMeasure.getExpr());
    }
}
