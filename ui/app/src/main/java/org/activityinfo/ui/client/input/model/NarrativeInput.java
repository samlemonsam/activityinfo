package org.activityinfo.ui.client.input.model;

import org.activityinfo.model.type.NarrativeValue;

public class NarrativeInput extends TextInput {

    @Override
    protected NarrativeValue createValue(String str) {
        return NarrativeValue.valueOf(str);
    }
}
