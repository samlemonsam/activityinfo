package org.activityinfo.ui.client.component.table.action;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.component.table.InstanceTable;
import org.activityinfo.ui.client.component.table.dialog.SubFormSelectDialog;

import java.util.ArrayList;
import java.util.List;

public class SubFormAction implements TableHeaderAction {


    interface Templates extends SafeHtmlTemplates {

        @Template("<button class=\"btn btn-default btn-xs dropdown-trigger\" type=\"button\" header_action=\"subforms\">" +
                "    {0}" +
                "    <span class=\"caret\"></span>" +
                "  </button>")
        SafeHtml dropdown(String label, SafeHtml listItems);

        @Template("<li><a href=\"{1}\">{0}</a></li>")
        SafeHtml listItem(String label, String link);
    }
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final InstanceTable table;

    private List<FormField> subFormFields;

    public SubFormAction(InstanceTable table) {
        this.table = table;
    }

    @Override
    public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
        subFormFields = new ArrayList<>();
        if(table.getRootFormClass() != null) {
            for (FormField field : table.getRootFormClass().getFields()) {
                if (field.getType() instanceof SubFormReferenceType) {
                    subFormFields.add(field);
                }
            }
        }

        if(!subFormFields.isEmpty()) {
            SafeHtmlBuilder list = new SafeHtmlBuilder();
            for (FormField subFormField : subFormFields) {
                list.append(TEMPLATES.listItem(subFormField.getLabel(), "#"));
            }
            sb.append(TEMPLATES.dropdown("Subforms", list.toSafeHtml()));
        }
    }

    @Override
    public void execute() {
        SubFormSelectDialog dialog = new SubFormSelectDialog(table.getEventBus(), subFormFields);
        dialog.show();

    }

    @Override
    public String getUniqueId() {
        return "subforms";
    }
}
