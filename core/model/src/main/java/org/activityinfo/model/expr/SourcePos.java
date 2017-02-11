package org.activityinfo.model.expr;

/**
 * Indicato
 */
public class SourcePos {
    private int line;
    private int column;

    public SourcePos(int line, int column) {
        this.line = line;
        this.column = column;
    }

    /**
     * @return the line index of this token
     */
    public int getLine() {
        return line;
    }

    /**
     *
     * @return the column index of this token from the beginning of the line.
     */
    public int getColumn() {
        return column;
    }

}
