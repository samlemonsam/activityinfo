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
import org.activityinfo.model.analysis.pivot.ImmutableMeasureModel;
import org.activityinfo.model.analysis.pivot.MeasureModel;
import org.activityinfo.model.analysis.pivot.Statistic;
import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.formulaDialog.FormulaDialog;
import org.activityinfo.ui.client.measureDialog.view.MeasureDialog;

import java.util.HashSet;
import java.util.Set;
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
        for (Statistic statistic : Statistic.values()) {
            CheckMenuItem aggregationItem = new CheckMenuItem(statistic.getLabel());
            aggregationItem.setChecked(measure.getStatistics().contains(statistic));
            aggregationItem.addCheckChangeHandler(event -> updateStatistic(measure, statistic, event.getChecked()));
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
            viewModel.getWorkingModel().withoutMeasure(id));
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
                viewModel.getWorkingModel().withMeasure(updatedMeasure));
    }


    private void updateMeasureFormula(MeasureModel measure, String formula) {
        ImmutableMeasureModel updatedMeasure = ImmutableMeasureModel.builder()
                .from(measure)
                .formula(formula)
                .build();

        viewModel.updateModel(
                viewModel.getWorkingModel().withMeasure(updatedMeasure));
    }


    private void updateStatistic(MeasureModel measureModel, Statistic statistic, Tree.CheckState checked) {

        Set<Statistic> newSelection = new HashSet<>(measureModel.getStatistics());
        if(checked == Tree.CheckState.CHECKED) {
            newSelection.add(statistic);
        } else {
            newSelection.remove(statistic);
        }

        ImmutableMeasureModel updatedMeasure = ImmutableMeasureModel.builder()
                .from(measureModel)
                .statistics(newSelection)
                .build();

        viewModel.updateModel(
                viewModel.getWorkingModel().withMeasure(updatedMeasure));
    }
}
