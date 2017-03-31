package org.activityinfo.ui.client.formulaDialog;

import org.activityinfo.model.expr.SourceRange;
import org.activityinfo.store.query.shared.NodeMatch;

public class FieldReference {
    private SourceRange sourceRange;
    private NodeMatch match;
    private String description;

    public FieldReference(SourceRange sourceRange, NodeMatch match) {
        this.sourceRange = sourceRange;
        this.match = match;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    String getDescription() {
        if(match.isEnumBoolean()) {
            return match.getFieldNode().getField().getLabel() + " is " + match.getEnumItem().getLabel();
        } else {
            return match.getFieldNode().getField().getLabel();
        }
    }

    public NodeMatch getMatch() {
        return match;
    }
}
