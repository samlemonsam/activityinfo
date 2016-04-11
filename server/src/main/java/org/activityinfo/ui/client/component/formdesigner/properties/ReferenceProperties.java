package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * Created by yuriyz on 4/11/2016.
 */
public class ReferenceProperties extends Composite {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, ReferenceProperties> {
    }

    @UiField
    HTMLPanel referenceGroup;
    @UiField
    ListBox referenceListBox;
    @UiField
    Button referenceAddButton;
    @UiField
    Button referenceRemoveButton;

    public ReferenceProperties() {
        uiBinder.createAndBindUi(this);
    }

    public HTMLPanel getReferenceGroup() {
        return referenceGroup;
    }

    public ListBox getReferenceListBox() {
        return referenceListBox;
    }

    public Button getReferenceAddButton() {
        return referenceAddButton;
    }

    public Button getReferenceRemoveButton() {
        return referenceRemoveButton;
    }
}
