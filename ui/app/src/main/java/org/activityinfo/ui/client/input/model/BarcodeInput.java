package org.activityinfo.ui.client.input.model;

import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.barcode.BarcodeValue;

public class BarcodeInput extends TextInput {

    @Override
    protected FieldValue createValue(String str) {
        return BarcodeValue.valueOf(str);
    }
}
