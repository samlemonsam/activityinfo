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
import com.sencha.gxt.widget.core.client.tree.Tree;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.analysis.model.DateLevel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.ImmutableDimensionModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.DimensionListItem;

import java.util.logging.Logger;

/**
 *
 */
public class DimensionPane implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(DimensionPane.class.getName());

    private AnalysisViewModel viewModel;
    private NewDimensionDialog dialog;
    private ContentPanel contentPanel;

    private ListStore<DimensionListItem> listStore;
    private ListView<DimensionListItem, DimensionListItem> listView;

    public DimensionPane(AnalysisViewModel viewModel) {
        this.viewModel = viewModel;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addDimensionClicked);

        listStore = new ListStore<>(DimensionListItem::getId);
        listView = new ListView<>(listStore,
                new IdentityValueProvider<>(),
                new PillCell<>(DimensionListItem::getLabel, this::onDimensionMenu));

        contentPanel = new ContentPanel();
        contentPanel.setHeading("Dimensions");
        contentPanel.addTool(addButton);
        contentPanel.setWidget(listView);

        viewModel.getDimensionListItems().subscribe(observable -> {
            listStore.clear();
            if (observable.isLoaded()) {
                LOGGER.info("Num dimension items = " + observable.get().size());

                listStore.replaceAll(observable.get());
            }
        });
    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    private void addDimensionClicked(SelectEvent event) {
        if (dialog == null) {
            dialog = new NewDimensionDialog(viewModel);
            dialog.addSelectionHandler(this::onNewDimensionSelected);
        }
        dialog.show();
    }

    private void onNewDimensionSelected(SelectionEvent<DimensionModel> event) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(event.getSelectedItem()));

        LOGGER.info("Num dimensions = " + viewModel.getModel().getDimensions());

    }


    private void onDimensionMenu(Element element, DimensionListItem dim) {

        Menu contextMenu = new Menu();


        // Edit the formula...
//        MenuItem editFormula = new MenuItem();
//        editFormula.setText("Edit Formula...");
//        editFormula.addSelectionHandler(event -> editFormula(dim));
//        editFormula.setEnabled(dim.getSourceModel() instanceof FieldDimensionSource);
//        contextMenu.add(editFormula);

        MenuItem editLabel = new MenuItem();
        editLabel.setText("Edit Label...");
        editLabel.addSelectionHandler(event -> editLabel(dim.getModel()));

        contextMenu.add(editLabel);

        contextMenu.add(new SeparatorMenuItem());

        // Allow choosing the date part to show
        if(dim.isDate()) {
            DateLevel currentLevel = dim.getModel().getDateLevel().orElse(null);
            for (DateLevel dateLevel : DateLevel.values()) {
                CheckMenuItem item = new CheckMenuItem(dateLevel.getLabel());
                item.setChecked(currentLevel == dateLevel);
                item.addSelectionHandler(event -> updateDateLevel(dim, DateLevel.YEAR));
                contextMenu.add(item);
            }

            contextMenu.add(new SeparatorMenuItem());

        }

        // Choose to include totals or not.
        CheckMenuItem totalsItem = new CheckMenuItem("Include Totals");
        totalsItem.setChecked(dim.getModel().getTotals());
        totalsItem.addCheckChangeHandler(event -> updateTotals(dim, event.getChecked()));
        contextMenu.add(totalsItem);
        contextMenu.add(new SeparatorMenuItem());

        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> removeDimension(dim.getId()));
        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }


    private void editLabel(DimensionModel dim) {
        PromptMessageBox messageBox = new PromptMessageBox("Update dimension's label:", "Enter the new label");
        messageBox.getTextField().setText(dim.getLabel());

        messageBox.addDialogHideHandler(event -> {
            if(event.getHideButton() == Dialog.PredefinedButton.OK) {
                updateLabel(dim, messageBox.getValue());
            }
        });

        messageBox.show();
    }

    private void updateLabel(DimensionModel dimension, String label) {
        viewModel.updateModel(viewModel.getModel().withDimension(
            ImmutableDimensionModel.builder()
                    .from(dimension)
                    .label(label)
                    .build()));
    }

    private void updateTotals(DimensionListItem dim, Tree.CheckState checkState) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(
                        ImmutableDimensionModel.builder()
                                .from(dim.getModel())
                                .totals(checkState == Tree.CheckState.CHECKED)
                                .build()));
    }

    private void updateDateLevel(DimensionListItem dim, DateLevel level) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(
                        ImmutableDimensionModel.builder()
                                .from(dim.getModel())
                                .dateLevel(level)
                                .build()));
    }

    private void removeDimension(String id) {
        viewModel.updateModel(
                viewModel.getModel().withoutDimension(id));
    }


}
