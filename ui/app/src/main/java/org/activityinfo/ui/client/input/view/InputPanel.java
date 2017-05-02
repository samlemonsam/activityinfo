package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;
import org.activityinfo.ui.client.store.FormStore;

public class InputPanel implements IsWidget, InputHandler {

    private final Observable<FormTree> formTree;

    private FieldPanel fieldPanel = null;

    private FormInputModel inputModel;
    private FormInputViewModelBuilder viewModelBuilder = null;


    private VerticalLayoutContainer container;

    public InputPanel(FormStore formStore, ResourceId formId) {

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


        if(fieldPanel == null) {
            viewModelBuilder = new FormInputViewModelBuilder(formTree.get());
            fieldPanel = new FieldPanel(formTree.get(), inputModel.getRecordRef(), this);
            container.add(fieldPanel, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
            container.forceLayout();

            FormInputViewModel viewModel = viewModelBuilder.build(inputModel);
            fieldPanel.update(viewModel);

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
        FormInputViewModel viewModel = viewModelBuilder.build(inputModel);
        fieldPanel.update(viewModel);
    }
}
