package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.core.shared.workflow.Workflow;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.ui.client.widget.legacy.MappingComboBox;
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
            if(Workflow.CLOSED_WORKFLOW_ID.equals(value)) {
                return closedWorkflow;
            } else {
                return openWorkflow;
            }
        }

        @Override
        public String convertFieldValue(Object value) {
            if(value == closedWorkflow) {
                return Workflow.CLOSED_WORKFLOW_ID;
            } else {
                return Workflow.OPEN_WORKFLOW_ID;
            }
        }
    }
}
