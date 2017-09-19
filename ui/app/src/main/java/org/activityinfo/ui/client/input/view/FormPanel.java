package org.activityinfo.ui.client.input.view;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
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

    private final CssFloatLayoutContainer panel;

    private final List<FieldView> fieldViews = new ArrayList<>();
    private final List<RepeatingSubFormPanel> subFormViews = new ArrayList<>();

    private InputHandler inputHandler;
    private RecordRef recordRef;

    public FormPanel(FormTree formTree, RecordRef recordRef, InputHandler inputHandler) {

        assert recordRef != null;

        InputResources.INSTANCE.style().ensureInjected();

        this.recordRef = recordRef;
        this.inputHandler = inputHandler;

        panel = new CssFloatLayoutContainer();
        panel.addStyleName(InputResources.INSTANCE.style().form());

        if(formTree.getRootFormClass().isSubForm()) {
            panel.addStyleName(InputResources.INSTANCE.style().subform());
        }

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                addSubForm(formTree, node);
            } else if(node.isParentReference()) {
                // ignore
            } else if(node.getField().isVisible()) {
                FieldWidget fieldWidget = createWidget(formTree, node.getField(), input -> onInput(node, input));

                if (fieldWidget != null) {
                    addField(node, fieldWidget);
                }
            }
        }
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }


    public void init(FormInputViewModel viewModel) {

        for (FieldView fieldView : fieldViews) {
            fieldView.init(viewModel);
        }

        for (RepeatingSubFormPanel subFormView : subFormViews) {
            subFormView.init(viewModel.getSubFormField(subFormView.getFieldId()));
        }
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

        HTML validationMessage = new HTML();
        validationMessage.setVisible(false);

        CssFloatLayoutContainer fieldPanel = new CssFloatLayoutContainer();
        fieldPanel.setStyleName(InputResources.INSTANCE.style().field());
        fieldPanel.add(fieldLabel, new CssFloatLayoutContainer.CssFloatData(1));
        fieldPanel.add(fieldWidget, new CssFloatLayoutContainer.CssFloatData(1, new Margins(5, 0, 5, 0)));
        fieldPanel.add(validationMessage, new CssFloatLayoutContainer.CssFloatData(1));
        panel.add(fieldPanel, new CssFloatLayoutContainer.CssFloatData(1, new Margins(10, 0, 10, 0)));

        fieldViews.add(new FieldView(node.getFieldId(), fieldWidget, validationMessage));
    }

    private void addSubForm(FormTree formTree, FormTree.Node node) {
        SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
        FormTree subTree = formTree.subTree(subFormType.getClassId());

        RepeatingSubFormPanel subPanel = new RepeatingSubFormPanel(node, subTree, inputHandler);

        panel.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
        subFormViews.add(subPanel);
    }


    @Override
    public Widget asWidget() {
        return panel;
    }

}
