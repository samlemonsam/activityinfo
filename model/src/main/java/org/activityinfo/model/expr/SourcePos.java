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

    public SourcePos(SourceRange a, SourceRange b) {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourcePos sourcePos = (SourcePos) o;

        if (line != sourcePos.line) return false;
        return column == sourcePos.column;

    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        return "SourcePos{" + line + ":" + column + "}";
    }
}
