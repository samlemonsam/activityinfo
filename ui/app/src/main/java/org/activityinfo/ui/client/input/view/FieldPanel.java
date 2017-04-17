package org.activityinfo.ui.client.input.view;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.view.field.FieldView;
import org.activityinfo.ui.client.input.view.field.FieldWidget;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.ui.client.input.view.field.FieldWidgetFactory.createWidget;

public class FieldPanel implements IsWidget {

    private final FormInputViewModelBuilder viewModelBuilder;
    private final FlowPanel panel;

    private final List<FieldView> fieldViews = new ArrayList<>();

    private FormInputModel inputModel;
    private FormInputViewModel viewModel;

    public FieldPanel(FormTree formTree) {

        InputResources.INSTANCE.style().ensureInjected();

        this.viewModelBuilder = new FormInputViewModelBuilder(formTree);

        panel = new FlowPanel();
        panel.addStyleName(InputResources.INSTANCE.style().form());

        for (FormTree.Node node : formTree.getRootFields()) {
            FieldWidget fieldWidget = createWidget(node.getType(), input -> onInput(node.getFieldId(), input));

            if(fieldWidget != null) {
                addField(node, fieldWidget);
            }
        }

        onInput(new FormInputModel());
    }

    private void onInput(ResourceId fieldId, FieldInput input) {
        onInput(inputModel.update(fieldId, input));
    }

    private void onInput(FormInputModel inputModel) {
        this.inputModel = inputModel;
        this.viewModel = viewModelBuilder.build(inputModel);

        // Update Field Views
        for (FieldView fieldView : fieldViews) {
            fieldView.getWidget().setRelevant(viewModel.isRelevant(fieldView.getFieldId()));
        }
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

    private HTML missingMessage() {
        return new HTML(SafeHtmlUtils.fromString(I18N.CONSTANTS.requiredFieldMessage()));
    }


    @Override
    public Widget asWidget() {
        return panel;
    }
}
