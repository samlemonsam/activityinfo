package org.activityinfo.ui.client.input.view;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.view.field.FieldView;
import org.activityinfo.ui.client.input.view.field.FieldWidget;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.ui.client.input.view.field.FieldWidgetFactory.createWidget;

/**
 * View that displays a form's fields and sub forms and accepts user input.
 */
public class FormPanel implements IsWidget {

    private final FlowPanel panel;

    private final List<FieldView> fieldViews = new ArrayList<>();
    private final List<RepeatingSubFormPanel> subFormViews = new ArrayList<>();

    private InputHandler inputHandler;
    private RecordRef recordRef;

    public FormPanel(FormTree formTree, RecordRef recordRef, InputHandler inputHandler) {
        InputResources.INSTANCE.style().ensureInjected();

        this.recordRef = recordRef;
        this.inputHandler = inputHandler;

        panel = new FlowPanel();
        panel.addStyleName(InputResources.INSTANCE.style().form());

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                addSubForm(formTree, node);
            } else if(node.isParentReference()) {
                // ignore
            } else {
                FieldWidget fieldWidget = createWidget(formTree, node.getType(), input -> onInput(node, input));

                if (fieldWidget != null) {
                    addField(node, fieldWidget);
                }
            }
        }
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }

    public void update(FormInputViewModel viewModel) {
        // Update Field Views
        for (FieldView fieldView : fieldViews) {
            fieldView.update(viewModel);
        }

        // Update Subforms
        for (RepeatingSubFormPanel subFormView : subFormViews) {
            subFormView.update(viewModel.getSubFormField(subFormView.getFieldId()));
        }

    }

    private void onInput(FormTree.Node node, FieldInput input) {
        inputHandler.updateModel(recordRef, node.getFieldId(), input);
    }

    private void addField(FormTree.Node node, FieldWidget fieldWidget) {

        Label fieldLabel = new Label(node.getField().getLabel());
        fieldLabel.addStyleName(InputResources.INSTANCE.style().fieldLabel());

        HTML missingMessage = missingMessage();
        missingMessage.setVisible(false);

        FlowPanel fieldPanel = new FlowPanel();
        fieldPanel.setStyleName(InputResources.INSTANCE.style().field());
        fieldPanel.add(fieldLabel);
        fieldPanel.add(fieldWidget);
        fieldPanel.add(missingMessage);
        panel.add(fieldPanel);

        fieldViews.add(new FieldView(node.getFieldId(), fieldWidget, missingMessage));
    }

    private void addSubForm(FormTree formTree, FormTree.Node node) {
        SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
        FormTree subTree = formTree.subTree(subFormType.getClassId());

        RepeatingSubFormPanel subPanel = new RepeatingSubFormPanel(node, subTree, inputHandler);

        panel.add(subPanel);
        subFormViews.add(subPanel);
    }

    private HTML missingMessage() {
        return new HTML(SafeHtmlUtils.fromString(I18N.CONSTANTS.requiredFieldMessage()));
    }


    @Override
    public Widget asWidget() {
        return panel;
    }

}
