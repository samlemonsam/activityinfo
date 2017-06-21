package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.CloseEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;
import org.activityinfo.ui.client.store.FormStore;

import java.util.logging.Logger;

/**
 * Root view for a {@link FormInputModel}
 */
public class FormInputView implements IsWidget, InputHandler {

    private static final Logger LOGGER = Logger.getLogger(FormInputView.class.getName());

    private final Observable<FormTree> formTree;

    private FormPanel formPanel = null;

    private FormStore formStore;
    private FormInputModel inputModel;
    private FormInputViewModelBuilder viewModelBuilder = null;

    private FormInputViewModel viewModel = null;

    private VerticalLayoutContainer container;


    public FormInputView(FormStore formStore, ResourceId formId) {
        this.formStore = formStore;

        inputModel = new FormInputModel(new RecordRef(formId, ResourceId.generateSubmissionId(formId)));
        this.formTree = formStore.getFormTree(formId);


        this.formTree.subscribe(this::onTreeChanged);

        container = new VerticalLayoutContainer();
    }

    private void onTreeChanged(Observable<FormTree> formTree) {
        if(formTree.isLoading()) {
            //  container.mask(I18N.CONSTANTS.loading());
            return;
        }

        if(formPanel == null) {
            viewModelBuilder = new FormInputViewModelBuilder(formStore, formTree.get());
            formPanel = new FormPanel(formTree.get(), inputModel.getRecordRef(), this);
            container.add(formPanel, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
            container.forceLayout();

            viewModel = viewModelBuilder.build(inputModel);
            formPanel.update(viewModel);


        } else {
            // Alert the user that the schema has been updated.
        }
    }


    @Override
    public Widget asWidget() {
        return container;
    }

    @Override
    public void updateModel(RecordRef recordRef, ResourceId fieldId, FieldInput value) {
        update(inputModel.update(recordRef, fieldId, value));
    }

    @Override
    public void addSubRecord(RecordRef subRecordRef) {
        update(inputModel.addSubRecord(subRecordRef));
    }

    private void update(FormInputModel updatedModel) {
        this.inputModel = updatedModel;
        this.viewModel = viewModelBuilder.build(inputModel);
        formPanel.update(viewModel);
    }

    public boolean isValid() {
        if(viewModel == null) {
            return false;
        }
        return false;
    }

    public void save(CloseEvent.CloseHandler closeHandler) {
        // If the view model is still loading, ignore this click
        if(viewModel == null) {
            return;
        }

        if(!viewModel.isValid()) {
            MessageBox box = new MessageBox(I18N.CONSTANTS.error(), I18N.CONSTANTS.pleaseFillInAllRequiredFields());
            box.setModal(true);
            box.show();
            return;
        }

        // Good to go...
        formStore.updateRecords(viewModel.buildTransaction()).then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                MessageBox box = new MessageBox(I18N.CONSTANTS.serverError(), I18N.CONSTANTS.errorOnServer());
                box.setModal(true);
                box.show();
            }

            @Override
            public void onSuccess(Void result) {
                closeHandler.onClose(new CloseEvent(null));
            }
        });
    }
}
