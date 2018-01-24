package org.activityinfo.ui.client.page.config.form;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.core.client.GWT;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.ui.client.page.config.design.AbstractDesignForm;
import org.activityinfo.ui.client.page.config.design.BlankValidator;

public class FolderForm extends AbstractDesignForm {

    public static final int PROJECT_MAX_LENGTH = 255;

    private FormBinding binding;
    private final TextField<String> nameField;

    public FolderForm() {
        super();

        binding = new FormBinding(this);

        UiConstants constants = GWT.create(UiConstants.class);

        nameField = new TextField<String>();
        nameField.setFieldLabel(constants.name());
        nameField.setMaxLength(PROJECT_MAX_LENGTH);
        nameField.setAllowBlank(false);
        nameField.setValidator(new BlankValidator());
        binding.addFieldBinding(new FieldBinding(nameField, "name"));
        this.add(nameField);
    }

    public TextField<String> getNameField() {
        return nameField;
    }

    public FormBinding getBinding() {
        return binding;
    }
}
