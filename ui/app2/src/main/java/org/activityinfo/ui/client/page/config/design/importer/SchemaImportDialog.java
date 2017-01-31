package org.activityinfo.ui.client.page.config.design.importer;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.style.BaseStylesheet;
import org.activityinfo.ui.client.widget.ModalDialog;
import org.activityinfo.ui.client.widget.ProgressBar;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaImportDialog {

    private static final Logger LOGGER = Logger.getLogger(SchemaImportDialog.class.getName());

    private enum State {
        /**
         * Initial, empty state, providing instructions to user
         */
        INIT,

        /**
         * The user has provided valid input
         */
        INPUT_VALID,

        /**
         * The user has provided invalid input
         */
        INPUT_INVALID,

        /**
         * There are warnings to display to the user
         * before continuing
         */
        IMPORT_WARNINGS,

        /**
         * Import is currently in progress
         */
        IMPORTING,

        /**
         * Import has succeeded
         */
        IMPORT_SUCCEEDED,

        /**
         * Import has failed
         */
        IMPORT_FAILED
    }

    private static ImportSchemaUiBinder uiBinder = GWT.create(ImportSchemaUiBinder.class);

    interface ImportSchemaUiBinder extends UiBinder<Widget, SchemaImportDialog> {
    }


    private Promise<Void> promise;

    private SchemaImporter importer;
    private SchemaImporterV2 importerV2;
    private SchemaImporterV3 importerV3;

    @UiField FlowPanel container;

    // first page
    @UiField HTMLPanel inputPanel;
    @UiField TextArea textArea;
    @UiField DivElement textAreaGroup;
    @UiField ParagraphElement textAreaHelp;

    // warning panel
    @UiField UListElement warningList;

    // second page
    @UiField ProgressBar progressBar;
    @UiField HTMLPanel progressPanel;
    @UiField HTMLPanel successPanel;
    @UiField HTMLPanel failurePanel;
    @UiField HTMLPanel warningPanel;
    @UiField ParagraphElement errorDescription;
    @UiField ScrollPanel warningScrollPanel;


    private ModalDialog dialog;

    private State currentState = State.INIT;

    private Timer validateTimer;

    public SchemaImportDialog(SchemaImporterV2 importerV2, SchemaImporterV3 importerV3) {
        this.importerV2 = importerV2;
        this.importerV3 = importerV3;

        BaseStylesheet.INSTANCE.ensureInjected();

        Widget content = uiBinder.createAndBindUi(this);

        dialog = new ModalDialog(content);
        dialog.setDialogTitle(I18N.CONSTANTS.importSchemaDialogTitle());
        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onPrimaryButtonClicked();
            }
        });
        dialog.getBackButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onBackButtonClicked();
            }
        });
    }


    public Promise<Void> show() {
        promise = new Promise<Void>();
        dialog.show();
        textArea.setFocus(true);
        return promise;
    }

    private void onPrimaryButtonClicked() {
        switch(currentState) {
            case INIT:
            case INPUT_INVALID:
                validateInput();
                break;
            case INPUT_VALID:
                if(importer.getWarnings().isEmpty()) {
                    startImport();
                } else {
                    showWarnings();
                }
                break;
            case IMPORT_WARNINGS:
                startImport();
                break;
            case IMPORT_SUCCEEDED:
                dialog.hide();
                break;
            case IMPORT_FAILED:
                startImport();
                break;
        }
    }

    private void onBackButtonClicked() {
        switch(currentState) {
            case IMPORT_WARNINGS:
                warningPanel.setVisible(false);
                break;
            case IMPORT_FAILED:
                failurePanel.setVisible(false);
                break;
        }
        inputPanel.setVisible(true);

        validateInput();

        dialog.getPrimaryButton().setText(I18N.CONSTANTS.ok());
        dialog.hideBackButton();
    }


    @UiHandler("textArea")
    public void onTextAreaChanged(ChangeEvent changeEvent) {
        validateInput();
    }

    @UiHandler("textArea")
    public void onKeyPressed(KeyPressEvent keyEvent) {
        scheduleInputValidation();
    }

    private void scheduleInputValidation() {
        if(validateTimer == null) {
            validateTimer = new Timer() {
                @Override
                public void run() {
                    validateInput();
                }
            };
        } else {
            validateTimer.cancel();
        }
        validateTimer.schedule(200);
    }

    private void validateInput() {
        if(isTextAreaEmpty()) {
            onInputInvalid(I18N.CONSTANTS.schemaImportEmpty());
        } else {
            try {
                tryValidateImport();
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Exception while validating input", e);
                onInputInvalid("Eek! Exception: " + e.getMessage());
            }
        }
    }

    private void tryValidateImport() {
        PastedTable source = new PastedTable(textArea.getText());
        if(source.getColumns().size() < 2 || source.getRows().size() <= 1) {
            onInputInvalid(I18N.CONSTANTS.invalidTableData());
            return;
        }

        if(importerV3.accept(source)) {
            importer = importerV3;
        } else {
            importer = importerV2;
        }

        if(!importer.parseColumns(source)) {
            onInputInvalid(I18N.MESSAGES.missingColumns(
                    Joiner.on(", ").join(importer.getMissingColumns())));
            return;
        }

        importer.processRows();
        onInputValid();
    }

    private void showWarnings() {
        currentState = State.IMPORT_WARNINGS;
        dialog.getBackButton().setVisible(true);
        dialog.getPrimaryButton().setText(I18N.CONSTANTS.ignoreImportWarnings());

        inputPanel.setVisible(false);
        warningPanel.setVisible(true);
        warningList.setInnerSafeHtml(composeWarnings());
    }

    private SafeHtml composeWarnings() {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        for(SafeHtml warning : importer.getWarnings()) {
            html.append(warning);
        }
        return html.toSafeHtml();
    }

    private boolean isTextAreaEmpty() {
        return Strings.nullToEmpty(textArea.getText()).trim().length() == 0;
    }

    private void onInputValid() {
        currentState = State.INPUT_VALID;
        textAreaGroup.removeClassName("has-error");
        textAreaGroup.addClassName("has-success");
        textAreaHelp.setInnerText(I18N.CONSTANTS.validSchemaImport());
    }

    private void onInputInvalid(String message) {
        currentState = State.INPUT_INVALID;
        textAreaGroup.addClassName("has-error");
        textAreaGroup.removeClassName("has-success");
        textAreaHelp.setInnerText(message);
        textArea.setFocus(true);
    }

    private void startImport() {
        currentState = State.IMPORTING;
        inputPanel.setVisible(false);
        warningPanel.setVisible(false);
        progressPanel.setVisible(true);
        dialog.hideBackButton();
        dialog.disablePrimaryButton();
        dialog.disableCancelButton();

        importer.clearWarnings();
        importer.processRows();

        importer.setProgressListener(new SchemaImporter.ProgressListener() {

            @Override
            public void submittingBatch(int batchNumber, int batchCount) {
                int percent = (int) (((double) batchNumber) / ((double) batchCount) * 100d);
                progressBar.setValue(percent);
            }
        });

        importer.persist(new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                promise.resolve(null);
                onImportSucceeded();
            }

            @Override
            public void onFailure(Throwable caught) {
                onImportFailed(caught);
            }
        });
    }

    private void onImportSucceeded() {
        currentState = State.IMPORT_SUCCEEDED;
        progressPanel.setVisible(false);
        successPanel.setVisible(true);
        dialog.hideCancelButton();
        dialog.getPrimaryButton().setEnabled(true);
        dialog.getPrimaryButton().setText(I18N.CONSTANTS.close());
    }

    private void onImportFailed(Throwable caught) {
        Log.error(caught.getMessage(), caught);

        currentState = State.IMPORT_FAILED;
        progressPanel.setVisible(false);
        failurePanel.setVisible(true);
        errorDescription.setInnerText(SafeHtmlUtils.htmlEscape(caught.getMessage()));
        dialog.getPrimaryButton().setEnabled(true);
        dialog.getPrimaryButton().setText(I18N.CONSTANTS.retry());
    }
}
