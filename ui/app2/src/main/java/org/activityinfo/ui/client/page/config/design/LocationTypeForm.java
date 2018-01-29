package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.ui.client.page.config.form.AbstractDesignForm;
import org.activityinfo.ui.client.widget.legacy.OnlyValidFieldBinding;

public class LocationTypeForm extends AbstractDesignForm {

    private FormBinding binding;

    public LocationTypeForm() {

        binding = new FormBinding(this);

        final NumberField idField = new NumberField();
        idField.setFieldLabel("ID");
        idField.setReadOnly(true);
        binding.addFieldBinding(new FieldBinding(idField, "id"));
        add(idField);

        TextField<String> nameField = new TextField<String>();
        nameField.setFieldLabel(I18N.CONSTANTS.name());
        nameField.setMaxLength(LocationTypeDTO.NAME_MAX_LENGTH);
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());

        binding.addFieldBinding(new OnlyValidFieldBinding(nameField, "name"));
        add(nameField);

        final Radio openWorkflow = new Radio();
        openWorkflow.setName("workflowId");
        openWorkflow.setBoxLabel(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.openWorkflow()));

        final Radio closedWorkflow = new Radio();
        closedWorkflow.setBoxLabel(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.closedWorkflow()));
        closedWorkflow.setName("workflowId");

        RadioGroup workflowGroup = new RadioGroup("workflowId");
        workflowGroup.setFieldLabel(I18N.CONSTANTS.permissions());
        workflowGroup.setOrientation(Style.Orientation.VERTICAL);
        workflowGroup.add(openWorkflow);
        workflowGroup.add(closedWorkflow);
        add(workflowGroup);

        FieldBinding workflowBinding = new FieldBinding(workflowGroup, "workflowId");
        workflowBinding.setConverter(new WorkflowIdConverter(closedWorkflow, openWorkflow));
        binding.addFieldBinding(workflowBinding);


        hideFieldWhenNull(idField);

    }

    @Override
    public FormBinding getBinding() {
        return binding;
    }

    private static class WorkflowIdConverter extends Converter {
        private final Radio closedWorkflow;
        private final Radio openWorkflow;

        public WorkflowIdConverter(Radio closedWorkflow, Radio openWorkflow) {
            this.closedWorkflow = closedWorkflow;
            this.openWorkflow = openWorkflow;
        }

        @Override
        public Radio convertModelValue(Object value) {
            if (LocationTypeDTO.CLOSED_WORKFLOW_ID.equals(value)) {
                return closedWorkflow;
            } else {
                return openWorkflow;
            }
        }

        @Override
        public String convertFieldValue(Object value) {
            if(value == closedWorkflow) {
                return LocationTypeDTO.CLOSED_WORKFLOW_ID;
            } else {
                return LocationTypeDTO.OPEN_WORKFLOW_ID;
            }
        }
    }
}
