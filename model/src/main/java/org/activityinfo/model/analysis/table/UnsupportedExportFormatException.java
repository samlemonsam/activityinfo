package org.activityinfo.model.analysis.table;

public class UnsupportedExportFormatException extends RuntimeException {

    UnsupportedExportFormatException() {
    }

    public UnsupportedExportFormatException(String format) {
        super("Export Format Type " + format + " is not currently supported.");
    }
}
