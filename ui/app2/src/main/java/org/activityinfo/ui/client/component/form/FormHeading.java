package org.activityinfo.ui.client.component.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;

/**
 * Displays the Form class label as header
 */
public class FormHeading implements IsWidget {

    private static FormHeadingBinder uiBinder = GWT
            .create(FormHeadingBinder.class);
    private final HTMLPanel panel;
    @UiField
    HeadingElement headerElement;

    @Override
    public Widget asWidget() {
        return panel;
    }

    interface FormHeadingBinder extends UiBinder<HTMLPanel, FormHeading> {
    }

    public FormHeading() {
        panel = uiBinder.createAndBindUi(this);
    }
    
    public void setFormClass(FormClass formClass) {
        headerElement.setInnerText(formClass.getLabel());
    }
}
