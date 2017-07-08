package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.dnd.core.client.DND;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.ListViewDragSource;
import com.sencha.gxt.dnd.core.client.ListViewDropTarget;
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
import org.activityinfo.ui.client.analysis.model.Axis;
import org.activityinfo.ui.client.analysis.model.DateLevel;
import org.activityinfo.ui.client.analysis.model.DimensionModel;
import org.activityinfo.ui.client.analysis.model.ImmutableDimensionModel;
import org.activityinfo.ui.client.analysis.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.analysis.viewModel.EffectiveDimension;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class DimensionPane implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(DimensionPane.class.getName());

    private AnalysisViewModel viewModel;
    private Axis axis;
    private NewDimensionDialog dialog;
    private ContentPanel contentPanel;

    private ListStore<EffectiveDimension> listStore;
    private ListView<EffectiveDimension, EffectiveDimension> listView;

    public DimensionPane(AnalysisViewModel viewModel, Axis axis) {
        this.viewModel = viewModel;
        this.axis = axis;

        ToolButton addButton = new ToolButton(ToolButton.PLUS);
        addButton.addSelectHandler(this::addDimensionClicked);

        listStore = new ListStore<>(EffectiveDimension::getId);
        listView = new ListView<>(listStore,
                new IdentityValueProvider<>(),
                new PillCell<>(EffectiveDimension::getLabel, this::onDimensionMenu));

        contentPanel = new ContentPanel();
        contentPanel.setHeading(axis.name());
        contentPanel.addTool(addButton);
        contentPanel.setWidget(listView);

        ListViewDragSource<EffectiveDimension> dragSource = new ListViewDragSource<>(listView);
        dragSource.setGroup("dims");

        ListViewDropTarget<EffectiveDimension> dropTarget = new ListViewDropTarget<EffectiveDimension>(listView) {
            @Override
            protected void onDragDrop(DndDropEvent event) {
                onDimensionsDropped(insertIndex, (List<EffectiveDimension>)event.getData());
            }
        };
        dropTarget.setGroup("dims");
        dropTarget.setFeedback(DND.Feedback.BOTH);
        dropTarget.setAllowSelfAsSource(true);


        viewModel.getDimensionListItems().subscribe(observable -> {
            listStore.clear();
            if (observable.isLoaded()) {
                LOGGER.info("Num dimension items = " + observable.get().size());

                List<EffectiveDimension> dims = new ArrayList<>();
                for (EffectiveDimension dim : observable.get()) {
                    if(dim.getAxis() == this.axis) {
                        dims.add(dim);
                    }
                }
                listStore.replaceAll(dims);
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

    private void onDimensionsDropped(int insertIndex, List<EffectiveDimension> dropped) {

        List<DimensionModel> dims = new ArrayList<>();
        for (EffectiveDimension effectiveDimension : dropped) {
            dims.add(ImmutableDimensionModel.builder()
                .from(effectiveDimension.getModel())
                .axis(this.axis)
                .build());
        }

        String afterDimId = null;
        EffectiveDimension afterItem = listStore.get(insertIndex);
        if(afterItem != null) {
            afterDimId = afterItem.getId();
        }

        viewModel.updateModel(
                viewModel.getModel().reorderDimensions(afterDimId, dims));
    }


    private void onNewDimensionSelected(SelectionEvent<DimensionModel> event) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(
                        ImmutableDimensionModel.builder()
                        .from(event.getSelectedItem())
                        .axis(this.axis)
                        .build()));

        LOGGER.info("Num dimensions = " + viewModel.getModel().getDimensions());

    }


    private void onDimensionMenu(Element element, EffectiveDimension dim) {

        Menu contextMenu = new Menu();


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

        boolean canTotal = canTotal(dim);

        // Choose to include totals or not.
        CheckMenuItem totalsItem = new CheckMenuItem("Include Totals");
        totalsItem.setChecked(dim.getModel().getTotals());
        totalsItem.addCheckChangeHandler(event -> updateTotals(dim, event.getChecked()));
        totalsItem.setEnabled(canTotal);
        contextMenu.add(totalsItem);

        CheckMenuItem percentagesItem = new CheckMenuItem("Include Percentages");
        percentagesItem.setChecked(dim.getModel().getPercentage());
        percentagesItem.addCheckChangeHandler(event -> updatePercentages(dim, event.getChecked()));
        percentagesItem.setEnabled(canTotal);
        contextMenu.add(percentagesItem);

        MenuItem totalsLabel = new MenuItem("Total Label...");
        totalsLabel.addSelectionHandler(event -> editTotalLabel(dim));
        totalsLabel.setEnabled(canTotal);
        contextMenu.add(totalsLabel);

        contextMenu.add(new SeparatorMenuItem());

        // Remove the dimension
        MenuItem remove = new MenuItem();
        remove.setText(I18N.CONSTANTS.remove());
        remove.addSelectionHandler(event -> removeDimension(dim.getId()));

        // Special handling for "Measures" and "Statistics" dimension
        if(dim.getId().equals(DimensionModel.MEASURE_ID)) {
            if(viewModel.getModel().getMeasures().size() > 1) {
                remove.setEnabled(false);
            }
        }

        if(dim.getId().equals(DimensionModel.STATISTIC_ID)) {
            if(viewModel.getModel().isMeasureDefinedWithMultipleStatistics()) {
                remove.setEnabled(false);
            }
        }

        contextMenu.add(remove);

        contextMenu.show(element, new Style.AnchorAlignment(Style.Anchor.BOTTOM, Style.Anchor.BOTTOM, true));
    }

    private boolean canTotal(EffectiveDimension dim) {
        switch (dim.getId()) {
            case DimensionModel.MEASURE_ID:
            case DimensionModel.STATISTIC_ID:
                return false;
            default:
                return true;
        }
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


    private void editTotalLabel(EffectiveDimension dim) {
        PromptMessageBox messageBox = new PromptMessageBox("Update total label:", "Enter the new label");
        messageBox.getTextField().setText(dim.getTotalLabel());

        messageBox.addDialogHideHandler(event -> {
            if(event.getHideButton() == Dialog.PredefinedButton.OK) {
                updateTotalLabel(dim, messageBox.getValue());
            }
        });
        messageBox.show();
    }

    private void updateTotalLabel(EffectiveDimension dim, String value) {
        viewModel.updateModel(viewModel.getModel().withDimension(
                ImmutableDimensionModel.builder()
                        .from(dim.getModel())
                        .totalLabel(value)
                        .build()));
    }

    private void updateTotals(EffectiveDimension dim, Tree.CheckState checkState) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(
                        ImmutableDimensionModel.builder()
                                .from(dim.getModel())
                                .totals(checkState == Tree.CheckState.CHECKED)
                                .build()));
    }

    private void updatePercentages(EffectiveDimension dim, Tree.CheckState checkState) {
        viewModel.updateModel(
                viewModel.getModel().withDimension(
                        ImmutableDimensionModel.builder()
                                .from(dim.getModel())
                                .percentage(checkState == Tree.CheckState.CHECKED)
                                .build()));
    }

    private void updateDateLevel(EffectiveDimension dim, DateLevel level) {
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
