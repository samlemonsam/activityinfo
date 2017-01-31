package org.activityinfo.ui.client.component.importDialog.model.source;

import org.activityinfo.model.type.FieldTypeClass;

/**
 * Describes a column in the imported table
 */
public class SourceColumn {

    private String header;
    private int index;
    private FieldTypeClass guessedType = FieldTypeClass.FREE_TEXT;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public FieldTypeClass getGuessedType() {
        return guessedType;
    }

    public void setGuessedType(FieldTypeClass guessedType) {
        this.guessedType = guessedType;
    }

    @Override
    public String toString() {
        return "[" + header + "]";
    }
}
