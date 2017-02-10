package org.activityinfo.ui.client.formulaDialog;

import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Status;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Observer;
import org.activityinfo.ui.client.store.FormStore;

import java.util.logging.Logger;

/**
 * Allows the user to build a formula
 */
public class FormulaDialog {

    private static final Logger LOGGER = Logger.getLogger(FormulaDialog.class.getName());

    private final ResourceId formId;
    private final Dialog dialog;
    private final FormulaEditor formulaArea;
    private final Status statusLabel;

    public FormulaDialog(FormStore formStore, ResourceId formId) {
        this.formId = formId;

        FormulaResources.INSTANCE.styles().ensureInjected();

        // First we have a list of fields that can be referenced
        LabelToolItem fieldHeader = new LabelToolItem(I18N.CONSTANTS.fields());
        ToolBar fieldBar = new ToolBar();
        fieldBar.add(fieldHeader);

        FieldList fieldList = new FieldList(formStore.getFormTree(formId));

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
        fieldPane.add(fieldList, new VerticalLayoutContainer.VerticalLayoutData(1, 1, margins));

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
        dialog.setPixelSize(640, 400);
        dialog.add(container);
        dialog.setModal(true);

        dialog.getButtonBar().insert(statusLabel, 0);
    }


    public void show() {
        this.dialog.show();
        this.dialog.center();

        formulaArea.getValue().subscribe(new Observer<ParsedFormula>() {
            @Override
            public void onChange(Observable<ParsedFormula> formula) {
                if(formula.isLoading()) {
                    statusLabel.setBusy(I18N.CONSTANTS.loading());
                    dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(false);
                } else {

                    LOGGER.info("Formula = " + formula.get().getRootNode().asExpression());

                    dialog.getButton(Dialog.PredefinedButton.OK).setEnabled(formula.get().isValid());
                    if(formula.get().isValid()) {
                        statusLabel.clearStatus(I18N.CONSTANTS.formulaValid());
                    } else {
                        statusLabel.clearStatus(formula.get().getErrorMessage());
                    }
                }
            }
        });
    }

    public void show(ExprNode expr) {
        formulaArea.setValue(expr.toString());
        show();
    }
}
