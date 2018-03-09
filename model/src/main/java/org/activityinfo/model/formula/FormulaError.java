package org.activityinfo.model.formula;

public class FormulaError {
    private SourceRange sourceRange;
    private String message;

    public FormulaError(SourceRange sourceRange, String message) {
        this.sourceRange = sourceRange;
        this.message = message;
    }

    public FormulaError(FormulaNode node, String message) {
        this(node.getSourceRange(), message);
    }

    public FormulaError(String message) {
        this.message = message;
        this.sourceRange = null;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasSourceRange() {
        return sourceRange != null;
    }
}
