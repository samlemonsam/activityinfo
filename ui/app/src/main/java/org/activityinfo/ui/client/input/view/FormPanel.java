package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.PeriodType;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.view.field.FieldView;
import org.activityinfo.ui.client.input.view.field.FieldWidget;
import org.activityinfo.ui.client.input.view.field.FieldWidgetFactory;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * View that displays a form's fields and sub forms and accepts user input.
 */
public class FormPanel implements IsWidget {

    private final FormSource formSource;

    private final CssFloatLayoutContainer panel;

    private final List<FieldView> fieldViews = new ArrayList<>();
    private final List<RepeatingSubFormPanel> repeatingSubForms = new ArrayList<>();
    private final List<KeyedSubFormPanel> keyedSubFormPanels = new ArrayList<>();

    private InputHandler inputHandler;
    private RecordRef recordRef;

    private int horizontalPadding = 0;

    private FormInputViewModel viewModel;

    public FormPanel(FormSource formSource, FormTree formTree, RecordRef recordRef, InputHandler inputHandler) {
        this.formSource = formSource;

        assert recordRef != null;

        InputResources.INSTANCE.style().ensureInjected();

        this.recordRef = recordRef;
        this.inputHandler = inputHandler;

        panel = new CssFloatLayoutContainer();
        panel.addStyleName(InputResources.INSTANCE.style().form());

        if(formTree.getRootFormClass().isSubForm()) {
            panel.addStyleName(InputResources.INSTANCE.style().subform());
        }

        FieldWidgetFactory widgetFactory = new FieldWidgetFactory(formSource, formTree);

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                if(node.isSubFormVisible()) {
                    addSubForm(formTree, node);
                }
            } else if(node.isParentReference()) {
                // ignore
            } else if(node.getField().isVisible() && !isSubFormKey(node)) {
                FieldWidget fieldWidget = widgetFactory.create(node.getField(), input -> onInput(node, input));

                if (fieldWidget != null) {
                    addField(node, fieldWidget);
                }
            }
        }

        if(formTree.getRootFormClass().isSubForm()) {
            TextButton deleteButton = new TextButton(I18N.CONSTANTS.remove());
            deleteButton.addSelectHandler(this::onDelete);
            panel.add(deleteButton, new CssFloatLayoutContainer.CssFloatData(1,
                    new Margins(0, horizontalPadding, 10, horizontalPadding)));
        }
    }

    private boolean isSubFormKey(FormTree.Node node) {
        return node.getDefiningFormClass().isSubForm() && node.getField().isKey() &&
                node.getType() instanceof PeriodType;
    }

    private void onDelete(SelectEvent event) {
        MessageBox messageBox = new MessageBox(I18N.CONSTANTS.confirmDeletion());
        messageBox.setMessage(I18N.CONSTANTS.confirmDeleteRecord());
        messageBox.setPredefinedButtons(Dialog.PredefinedButton.OK, Dialog.PredefinedButton.CANCEL);
        messageBox.getButton(Dialog.PredefinedButton.OK)
                .addSelectHandler(e -> inputHandler.deleteSubRecord(this.recordRef));
        messageBox.show();
    }

    public RecordRef getRecordRef() {
        return recordRef;
    }


    public void init(FormInputViewModel viewModel) {

        this.recordRef = viewModel.getRecordRef();

        for (FieldView fieldView : fieldViews) {
            fieldView.init(viewModel);
        }

        for (RepeatingSubFormPanel subFormView : repeatingSubForms) {
            subFormView.init(viewModel.getSubForm(subFormView.getFieldId()));
        }
    }

    public void updateView(FormInputViewModel viewModel) {

        this.viewModel = viewModel;

        // Update Field Views
        for (FieldView fieldView : fieldViews) {
            fieldView.updateView(viewModel);
        }

        // Update Subforms
        for (RepeatingSubFormPanel subFormView : repeatingSubForms) {
            subFormView.updateView(viewModel.getSubForm(subFormView.getFieldId()));
        }
        for (KeyedSubFormPanel subFormView : keyedSubFormPanels) {
            subFormView.updateView(viewModel.getSubForm(subFormView.getFieldId()));
        }

    }

    private void onInput(FormTree.Node node, FieldInput input) {
        if(viewModel.isPlaceholder()) {
            inputHandler.updateSubModel(viewModel.getInputModel().update(node.getFieldId(), input));

        } else {
            inputHandler.updateModel(recordRef, node.getFieldId(), input);
        }
    }

    private void addField(FormTree.Node node, FieldWidget fieldWidget) {

        Label fieldLabel = new Label(node.getField().getLabel());
        fieldLabel.addStyleName(InputResources.INSTANCE.style().fieldLabel());

        HTML validationMessage = new HTML();
        validationMessage.setVisible(false);

        CssFloatLayoutContainer fieldPanel = new CssFloatLayoutContainer();
        fieldPanel.setStyleName(InputResources.INSTANCE.style().field());
        fieldPanel.add(fieldLabel, new CssFloatLayoutContainer.CssFloatData(1));
        fieldPanel.add(fieldWidget, new CssFloatLayoutContainer.CssFloatData(1,
                new Margins(5, horizontalPadding, 5, horizontalPadding)));
        fieldPanel.add(validationMessage, new CssFloatLayoutContainer.CssFloatData(1));
        panel.add(fieldPanel, new CssFloatLayoutContainer.CssFloatData(1,
                new Margins(10, horizontalPadding, 10, horizontalPadding)));

        fieldViews.add(new FieldView(node.getFieldId(), fieldWidget, validationMessage));
    }

    private void addSubForm(FormTree formTree, FormTree.Node node) {
        SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
        FormTree subTree = formTree.subTree(subFormType.getClassId());
        SubFormKind subFormKind = subTree.getRootFormClass().getSubFormKind();

        if(subFormKind == SubFormKind.REPEATING) {
            RepeatingSubFormPanel subPanel = new RepeatingSubFormPanel(formSource, node, subTree, inputHandler);

            panel.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
            repeatingSubForms.add(subPanel);

        } else {
            KeyedSubFormPanel subPanel = new KeyedSubFormPanel(recordRef, formSource, node, subTree, inputHandler);
            panel.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
            keyedSubFormPanels.add(subPanel);
        }
    }

    public void setBorders(boolean borders) {
        panel.setBorders(borders);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

}
