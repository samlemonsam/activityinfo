package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.enumerated.EnumType;

/**
 * Allows the user to edit the properties of a quantity type
 */
public class EnumTypeEditor extends TypeEditor<EnumType> {

    interface EnumTypeEditorPanelUiBinder extends UiBinder<FlowPanel, EnumTypeEditor> {
    }

    private static EnumTypeEditorPanelUiBinder ourUiBinder = GWT.create(EnumTypeEditorPanelUiBinder.class);


    private final FlowPanel panel;

    @UiField
    RadioButton automaticPresentation;
    @UiField
    RadioButton checkboxPresentation;
    @UiField
    RadioButton dropdownPresentation;


    public EnumTypeEditor() {
        this.panel = ourUiBinder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    @Override
    protected boolean accept(FieldType type) {
        return type instanceof EnumType && ((EnumType) type).getCardinality() == Cardinality.SINGLE;
    }

    @Override
    protected void show(EnumType type) {
        automaticPresentation.setValue(type.getPresentation() == EnumType.Presentation.AUTOMATIC);
        checkboxPresentation.setValue(type.getPresentation() == EnumType.Presentation.RADIO_BUTTON);
        dropdownPresentation.setValue(type.getPresentation() == EnumType.Presentation.DROPDOWN);
    }

    @UiHandler("automaticPresentation")
    void onAutomaticSelected(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.AUTOMATIC);
    }

    @UiHandler("checkboxPresentation")
    void onCheckBoxPresentation(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.RADIO_BUTTON);
    }

    @UiHandler("dropdownPresentation")
    void onDropdownPresentation(ValueChangeEvent<Boolean> event) {
        updatePresentation(EnumType.Presentation.DROPDOWN);
    }

    private void updatePresentation(EnumType.Presentation presentation) {
        updateType(currentType().withPresentation(presentation));
    }
}