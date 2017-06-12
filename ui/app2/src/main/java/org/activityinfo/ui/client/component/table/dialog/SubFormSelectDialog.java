package org.activityinfo.ui.client.component.table.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.NavigationEvent;
import org.activityinfo.ui.client.page.NavigationHandler;
import org.activityinfo.ui.client.page.resource.ResourcePage;
import org.activityinfo.ui.client.page.resource.ResourcePlace;
import org.activityinfo.ui.client.widget.ModalDialog;

import java.util.List;

public class SubFormSelectDialog {


    private final ModalDialog dialog;


    interface SubFormSelectDialogUiBinder extends UiBinder<HTMLPanel, SubFormSelectDialog> {
    }

    private static SubFormSelectDialogUiBinder uiBinder = GWT.create(SubFormSelectDialogUiBinder.class);
    @UiField
    ListBox subFormList;

    public SubFormSelectDialog(final EventBus eventBus, final List<FormField> subFormFields) {
        dialog = new ModalDialog(uiBinder.createAndBindUi(this));
        dialog.setDialogTitle("Select Sub Form");
        dialog.getPrimaryButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                FormField subFormField = subFormFields.get(subFormList.getSelectedIndex());
                SubFormReferenceType type = (SubFormReferenceType) subFormField.getType();

                eventBus.fireEvent(new NavigationEvent(
                        NavigationHandler.NAVIGATION_REQUESTED,
                        new ResourcePlace(type.getClassId(), ResourcePage.TABLE_PAGE_ID)));
                dialog.hide();
            }
        });

        for (FormField subFormField : subFormFields) {
            subFormList.addItem(subFormField.getLabel());
        }
        subFormList.setVisibleItemCount(subFormFields.size());
        subFormList.setSelectedIndex(0);


    }

    public void show() {
        dialog.show();
    }
}
