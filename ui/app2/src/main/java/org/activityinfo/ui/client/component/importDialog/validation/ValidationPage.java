package org.activityinfo.ui.client.component.importDialog.validation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseMonitor;
import org.activityinfo.ui.client.component.importDialog.ImportPage;
import org.activityinfo.ui.client.component.importDialog.Importer;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidatedRowTable;

/**
 * Presents the result of the matching to the user and provides
 * and opportunity to resolve conversion problems or ambiguities
 * in reference instances.
 */
public class ValidationPage extends Composite implements PromiseMonitor, ImportPage {

    private static ValidationPageUiBinder uiBinder = GWT
            .create(ValidationPageUiBinder.class);


    interface ValidationPageUiBinder extends UiBinder<Widget, ValidationPage> {
    }

    private ImportModel model;
    private Importer importer;

    @UiField(provided = true)
    ValidationGrid dataGrid;

    @UiField
    Element loadingElement;
    @UiField
    Element loadingErrorElement;
    @UiField
    Anchor retryLink;

    public ValidationPage(ImportModel model, Importer importer) {
        this.model = model;
        this.importer = importer;

        ValidationPageStyles.INSTANCE.ensureInjected();

        dataGrid = new ValidationGrid();

        initWidget(uiBinder.createAndBindUi(this));
        
        retryLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                clickEvent.preventDefault();
                start();
            }
        });
    }

    @Override
    public void start() {
        importer.validateRows(model)
                .withMonitor(this)
                .then(new AsyncCallback<ValidatedRowTable>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.error("Rows validation failed", throwable);
                    }

                    @Override
                    public void onSuccess(ValidatedRowTable input) {
                        dataGrid.refresh(input);
                    }
                });
    }

    @Override
    public void fireStateChanged() {
    }

    public int getInvalidRowsCount() {
        return dataGrid.getInvalidRowsCount();
    }

    @Override
    public void onPromiseStateChanged(Promise.State state) {
        this.loadingElement.getStyle().setDisplay(  state == Promise.State.PENDING ?
                Style.Display.BLOCK : Style.Display.NONE );
        this.loadingErrorElement.getStyle().setDisplay(  state == Promise.State.REJECTED ?
                Style.Display.BLOCK : Style.Display.NONE );
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean hasNextStep() {
        return false;
    }

    @Override
    public boolean hasPreviousStep() {
        return false;
    }

    @Override
    public void nextStep() {

    }

    @Override
    public void previousStep() {

    }

}
