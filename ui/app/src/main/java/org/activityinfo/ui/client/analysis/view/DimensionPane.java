package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.AnalysisModel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.DimensionSourceModel;

/**
 *
 */
public class DimensionPane implements IsWidget {

    private AnalysisModel model;
    private NewDimensionDialog dialog;
    private ContentPanel contentPanel;

    private ListStore<DimensionModel> listStore;
    private ListView<DimensionModel, DimensionModel> listView;

    public DimensionPane(AnalysisModel model) {
        this.model = model;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addDimensionClicked);

        listStore = new ListStore<>(DimensionModel::getId);
        listView = new ListView<>(listStore,
                new IdentityValueProvider<>(),
                new PillCell<>(DimensionModel::getLabel, this::onDimensionMenu));

        contentPanel = new ContentPanel();
        contentPanel.setHeading("Dimensions");
        contentPanel.addTool(addButton);
        contentPanel.setWidget(listView);

        model.getDimensions().asObservable().subscribe(observable -> {
            if (observable.isLoaded()) {
                listStore.replaceAll(observable.get());
            } else {
                listStore.clear();
            }
        });
    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    private void addDimensionClicked(SelectEvent event) {
        if (dialog == null) {
            dialog = new NewDimensionDialog(model);
            dialog.addSelectionHandler(this::onNewDimensionSelected);
        }
        dialog.show();
    }

    private void onNewDimensionSelected(SelectionEvent<DimensionSourceModel> event) {
        model.addDimension(event.getSelectedItem());
    }


    private void onDimensionMenu(Element element, DimensionModel dim) {

        Menu contextMenu = new Menu();


        // Edit the formula...
//        MenuItem editFormula = new MenuItem();
//        editFormula.setText("Edit Formula...");
//        editFormula.addSelectionHandler(event -> editFormula(dim));
//        editFormula.setEnabled(dim.getSourceModel() instanceof FieldDimensionSource);
//        contextMenu.add(editFormula);

        // Allow choosing the date part to show
        MenuItem year = new CheckMenuItem("Year");
        MenuItem quarter = new CheckMenuItem("Quarter");
        MenuItem month = new CheckMenuItem("Month");
        MenuItem day = new CheckMenuItem("Day");

        contextMenu.add(year);
        contextMenu.add(quarter);
        contextMenu.add(month);
        contextMenu.add(day);

        contextMenu.add(new SeparatorMenuItem());


        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> model.removeDimension(dim.getId()));
        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }


}
