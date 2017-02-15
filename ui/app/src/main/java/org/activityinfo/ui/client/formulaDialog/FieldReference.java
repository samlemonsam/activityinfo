package org.activityinfo.ui.client.formulaDialog;

import org.activityinfo.model.expr.SourceRange;

public class FieldReference {
    private SourceRange sourceRange;
    private String description;

    public FieldReference(SourceRange sourceRange, String description) {
        this.sourceRange = sourceRange;
        this.description = description;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public String getDescription() {
        return description;
    }
}
