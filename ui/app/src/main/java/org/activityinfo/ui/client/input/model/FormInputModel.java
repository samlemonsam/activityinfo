package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;

public class FormInputModel {

    private final HashMap<ResourceId, FieldInput> fieldInputs;

    public FormInputModel() {
        fieldInputs = new HashMap<>();
    }

    private FormInputModel(HashMap<ResourceId, FieldInput> fieldInputs) {
        this.fieldInputs = new HashMap<>(fieldInputs);
    }

    public FieldInput get(ResourceId fieldId) {
        FieldInput fieldInput = fieldInputs.get(fieldId);
        if(fieldInput == null) {
            return FieldInput.EMPTY;
        }
        return fieldInput;
    }

    public FormInputModel update(ResourceId fieldId, FieldInput input) {
        FormInputModel newModel = new FormInputModel(this.fieldInputs);
        newModel.fieldInputs.put(fieldId, input);
        return newModel;
    }
}
