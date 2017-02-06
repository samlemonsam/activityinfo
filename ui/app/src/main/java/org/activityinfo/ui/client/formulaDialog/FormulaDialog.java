package org.activityinfo.ui.client.formulaDialog;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.toolbar.LabelToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.codemirror.client.CodeMirror;
import org.activityinfo.ui.codemirror.client.CodeMirrorWidget;
import org.activityinfo.ui.codemirror.client.Linter;
import org.activityinfo.ui.codemirror.client.LintingProblem;

import java.util.logging.Logger;

/**
 * Allows the user to build a formula
 */
public class FormulaDialog {

    private static final Logger LOGGER = Logger.getLogger(FormulaDialog.class.getName());

    private final ResourceId formId;
    private final Dialog dialog;

    public FormulaDialog(FormStore formStore, ResourceId formId) {
        this.formId = formId;

        // First we have a list of fields that can be referenced
        LabelToolItem fieldHeader = new LabelToolItem(I18N.CONSTANTS.fields());
        ToolBar fieldBar = new ToolBar();
        fieldBar.add(fieldHeader);

        FieldList fieldList = new FieldList(formStore.getFormTree(formId));

        // Then the actual formula
        LabelToolItem formulaHeader = new LabelToolItem(I18N.CONSTANTS.formula());
        ToolBar formulaBar = new ToolBar();
        formulaBar.add(formulaHeader);

        Linter linter = new Linter() {
            @Override
            public LintingProblem[] lint(String text) {
                if(text.startsWith("BAD")) {
                    LintingProblem problem = new LintingProblem();
                    problem.setMessage("Bad text found!");
                    problem.setFrom(CodeMirror.create(0, 0));
                    problem.setFrom(CodeMirror.create(0, 3));
                    return new LintingProblem[]{problem};
                } else {
                    return new LintingProblem[0];
                }
            }
        };

        CodeMirrorWidget formulaArea = new CodeMirrorWidget(linter);
        formulaArea.addValueChangeHandler(change -> {
            LOGGER.info("Formula: " + change.getValue());
        });

        Margins margins = new Margins(0, 10, 0, 10);

        VerticalLayoutContainer container = new VerticalLayoutContainer();
        container.add(fieldBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1, margins));
        container.add(fieldList, new VerticalLayoutContainer.VerticalLayoutData(1, 0.5, margins));

        container.add(formulaBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1, margins));
        container.add(formulaArea, new VerticalLayoutContainer.VerticalLayoutData(1, 0.5, margins));

        dialog = new Dialog();
        dialog.setHeading("Formula");
        dialog.setClosable(true);
        dialog.setResizable(true);
        dialog.setPixelSize(640, 400);
        dialog.add(container);
    }


    public void show() {
        this.dialog.show();
        this.dialog.center();
    }

}
