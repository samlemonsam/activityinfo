package org.activityinfo.model.formula;


public class SourceRange {
    private final SourcePos start;
    private final SourcePos end;

    public SourceRange(SourcePos start, int length) {
        this.start = start;
        this.end = new SourcePos(start.getLine(), start.getColumn() + length);
    }

    public SourceRange(SourcePos start, SourcePos end) {
        this.start = start;
        this.end = end;
    }

    public SourceRange(Token token) {
        this(token.getStart(), token.getLength());
    }

    public SourceRange(Token startToken, Token endToken) {
        this(startToken.getStart(), endToken.getEnd());
    }

    public SourceRange(SourceRange start, SourceRange end) {
        this(start.getStart(), end.getEnd());
    }

    public SourcePos getStart() {
        return start;
    }

    public SourcePos getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceRange that = (SourceRange) o;

        if (!start.equals(that.start)) return false;
        return end.equals(that.end);

    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SourceRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
