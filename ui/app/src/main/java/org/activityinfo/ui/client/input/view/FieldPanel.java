package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;
import org.activityinfo.ui.client.input.view.field.FieldView;
import org.activityinfo.ui.client.input.view.field.FieldWidget;
import org.activityinfo.ui.client.input.view.field.FieldWidgetFactory;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModelBuilder;

import java.util.ArrayList;
import java.util.List;

public class FieldPanel extends Widget {

    private final FormInputViewModelBuilder viewModelBuilder;
    private final VerticalLayoutContainer container;

    private final List<FieldView> fieldViews = new ArrayList<>();

    private FormInputModel inputModel;
    private FormInputViewModel viewModel;

    public FieldPanel(FormTree formTree) {

        InputResources.INSTANCE.style().ensureInjected();

        this.viewModelBuilder = new FormInputViewModelBuilder(formTree);

        container = new VerticalLayoutContainer();
        container.setScrollMode(ScrollSupport.ScrollMode.AUTOY);

        for (FormTree.Node node : formTree.getRootFields()) {
            FieldWidget fieldWidget = FieldWidgetFactory.create(node.getType(),
                    input -> onInput(node.getFieldId(), input));

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

        VerticalLayoutContainer.VerticalLayoutData fieldWidgetLayout = new VerticalLayoutContainer.VerticalLayoutData(1.0, -1);

        container.add(fieldLabel);
        container.add(fieldWidget, fieldWidgetLayout);
        fieldViews.add(new FieldView(node.getFieldId(), fieldWidget));
    }


}
