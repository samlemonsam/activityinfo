package org.activityinfo.server.endpoint.odk;

import org.activityinfo.model.type.barcode.BarcodeValue;


/**
 * BarcodeFieldValueParser.
 *
 * @author Mithun<shahinur.bd@gmail.com>
 */
class BarcodeFieldValueParser implements FieldValueParser {
    @Override
    public BarcodeValue parse(String text) {
        if (text == null) throw new IllegalArgumentException("Malformed Element passed to OdkFieldValueParser.parse()");

        return BarcodeValue.valueOf(text);
    }
}
