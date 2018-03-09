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
package org.activityinfo.ui.client.formulaDialog;

import com.google.common.base.Strings;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Status;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.store.FormStore;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Allows the user to build a formula
 */
public class FormulaDialog {

    private static final Logger LOGGER = Logger.getLogger(FormulaDialog.class.getName());

    private final EventBus eventBus = new SimpleEventBus();

    private final ResourceId formId;
    private final Dialog dialog;
    private final FormulaEditor formulaArea;
    private final Status statusLabel;

    private Subscription formulaSubscription;
    private final FieldList formulaTree;

    private Consumer<ParsedFormula> handler;

    public FormulaDialog(FormStore formStore, ResourceId formId) {
        this.formId = formId;

        FormulaResources.INSTANCE.styles().ensureInjected();

        // First we have a list of fields that can be referenced
        LabelToolItem fieldHeader = new LabelToolItem(I18N.CONSTANTS.fields());
        ToolBar fieldBar = new ToolBar();
        fieldBar.add(fieldHeader);

        formulaTree = new FieldList(formStore.getFormTree(formId));
        formulaTree.addSelectionHandler(this::onFormulaElementDoubleClicked);

        // Then the actual formula
        LabelToolItem formulaHeader = new LabelToolItem(I18N.CONSTANTS.formula());
        ToolBar formulaBar = new ToolBar();
        formulaBar.add(formulaHeader);

        formulaArea = new FormulaEditor(formStore.getFormTree(formId));

        // Initializing dragging for Formula Area
        new FormulaDropTarget(formulaArea);

        statusLabel = new Status();
        statusLabel.setWidth(200);

        Margins margins = new Margins(0, 10, 0, 10);

        VerticalLayoutContainer fieldPane = new VerticalLayoutContainer();
        fieldPane.add(fieldBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1, margins));
        fieldPane.add(formulaTree, new VerticalLayoutContainer.VerticalLayoutData(1, 1, margins));

        VerticalLayoutContainer formulaPane = new VerticalLayoutContainer();
        formulaPane.add(formulaBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1, margins));
        formulaPane.add(formulaArea, new VerticalLayoutContainer.VerticalLayoutData(1, 1, margins));

        HorizontalLayoutContainer container = new HorizontalLayoutContainer();
        container.add(formulaPane, new HorizontalLayoutContainer.HorizontalLayoutData(0.6, 1));
        container.add(fieldPane, new HorizontalLayoutContainer.HorizontalLayoutData(0.4, 1));

        dialog = new Dialog();
        dialog.setHeading("Formula");
        dialog.setClosable(true);
        dialog.setResizable(true);
        dialog.setPixelSize(
                (int)(Window.getClientWidth() * 0.9),
                (int)(Window.getClientHeight() * 0.9));
        dialog.add(container);
        dialog.setModal(true);
        dialog.getButtonBar().insert(statusLabel, 0);

        dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(this::onOk);
        dialog.addHideHandler(this::onHide);
    }

    private void onFormulaElementDoubleClicked(SelectionEvent<FormulaElement> event) {
        formulaArea.insertAt(event.getSelectedItem().getExpr().asExpression(),
                formulaArea.getEditor().getDoc().getCursor());
    }


    public void show(String formulaString, Consumer<ParsedFormula> handler) {
        formulaArea.setValue(Strings.nullToEmpty(formulaString));
        this.dialog.show();
        this.dialog.center();
        this.handler = handler;
        formulaTree.connect();
        formulaSubscription = formulaArea.getValue().subscribe(formula -> {
            if(formula.isLoading()) {
                statusLabel.setBusy(I18N.CONSTANTS.loading());
                dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(false);
            } else {
                dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(formula.get().isValid());
                if(formula.get().isValid()) {
                    statusLabel.clearStatus(I18N.CONSTANTS.formulaValid());
                    LOGGER.info("Formula = " + formula.get().getRootNode().asExpression());
                } else {
                    statusLabel.clearStatus(formula.get().getErrorMessage());
                }
            }
        });
    }

    private void onOk(SelectEvent event) {
        Observable<ParsedFormula> result = formulaArea.getValue();
        if(result.isLoaded() && result.get().isValid()) {
            dialog.hide();
            handler.accept(result.get());
            handler = null;
        }
    }


    private void onHide(HideEvent hideEvent) {
        formulaSubscription.unsubscribe();
        formulaSubscription = null;
    }
}
