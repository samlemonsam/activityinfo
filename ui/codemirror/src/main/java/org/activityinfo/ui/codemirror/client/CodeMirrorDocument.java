package org.activityinfo.ui.codemirror.client;

import jsinterop.annotations.JsType;

/**
 * Interface to the CodeMirrorDocument
 */
@JsType(isNative = true)
public interface CodeMirrorDocument {

    /**
     * Get the current editor content.
     */
    String getValue();

    /**
     * Get the current editor content. You can pass it an optional argument to specify the
     * string to be used to separate lines (defaults to "\n").
     */
    String getValue(String seperator);


    /**
     * Get the text between the given points in the editor, which should be {line, ch} objects.
     * An optional third argument can be given to indicate the line separator string to use (defaults to "\n").
     */
    String getRange(Pos from, Pos to, String seperator);

    /**
     * Replace the part of the document between from and to with the given string. from and to must be {line, ch}
     * objects. to can be left off to simply insert the string at position from. When origin is given, it will
     * be passed on to "change" events, and its first letter will be used to determine whether this change
     * can be merged with previous history events, in the way described for selection origins.
     */
    void replaceRange(String replacement, Pos from, Pos to, String origin);

    /**
     * Replace the part of the document between from and to with the given string. from and to must be {line, ch}
     * objects. to can be left off to simply insert the string at position from. When origin is given, it will
     * be passed on to "change" events, and its first letter will be used to determine whether this change
     * can be merged with previous history events, in the way described for selection origins.
     */
    void replaceRange(String replacement, Pos from, Pos to);


    /**
     * Get the content of line n.
     */
    String getLine(int n);

    /**
     * Get the number of lines in the editor.
     */
    int lineCount();

    /**
     * Get the first line of the editor. This will usually be zero but for linked sub-views,
     * or documents instantiated with a non-zero first line, it might return other values.
     */
    int firstLine();

    /**
     * Get the last line of the editor. This will usually be doc.lineCount() - 1, but for
     * linked sub-views, it might return other values.
     */
    int lastLine();

    /**
     * Fetches the line handle for the given line number.
     */
    LineHandle getLineHandle(int num);

    /**
     * Given a line handle, returns the current position of that line (or null when it is no longer in the document).
     */
    int getLineNumber(LineHandle handle);


    /**
     * Returns a number that can later be passed to isClean to test whether any edits were made (and not undone) in
     * the meantime. If closeEvent is true, the current history event will be ‘closed’, meaning it can't
     * be combined with further changes (rapid typing or deleting events are typically combined).
     */
    int changeGeneration();

    /**
     * Returns whether the document is currently clean — not modified since initialization or the last call to
     * markClean if no argument is passed, or since the matching call to changeGeneration if a generation value is given.
     */
    boolean isClean(int generation);

    void setValue(String value);

    /**
     * Get the currently selected code. Optionally pass a line separator to put between the lines in the output.
     * When multiple selections are present, they are concatenated with instances of lineSep in between.
     */
    String getSelection(String lineSep);


    /**
     * Get the currently selected code. Optionally pass a line separator to put between the lines in the output.
     * When multiple selections are present, they are concatenated with instances of lineSep in between.
     */
    String getSelection();

    /**
     * Replace the selection(s) with the given string. By default, the new selection ends up after the inserted text.
     * The optional select argument can be used to change this—passing "around" will cause the new text to
     * be selected, passing "start" will collapse the selection to the start of the inserted text.
     */
    String replaceSelection(String replacement, String select);


    /**
     * Replace the selection(s) with the given string. By default, the new selection ends up after the inserted text.
     * The optional select argument can be used to change this—passing "around" will cause the new text to
     * be selected, passing "start" will collapse the selection to the start of the inserted text.
     */
    String replaceSelection(String replacement);

    /**
     * Retrieve one end of the primary selection. start is an optional string indicating which end of the
     * selection to return. It may be "from", "to", "head" (the side of the selection that moves when you
     * press shift+arrow), or "anchor" (the fixed side of the selection).
     * Omitting the argument is the same as passing "head". A {line, ch} object will be returned.
     */
    Pos getCursor();

    /**
     * Retrieve one end of the primary selection. start is an optional string indicating which end of the
     * selection to return. It may be "from", "to", "head" (the side of the selection that moves when you
     * press shift+arrow), or "anchor" (the fixed side of the selection).
     * Omitting the argument is the same as passing "head". A {line, ch} object will be returned.
     */
    Pos getCursor(String start);


    /**
     * Return true if any text is selected.
     */
    boolean somethingSelected();

    /**
     * Set the cursor position. You can either pass a single {line, ch} object, or the line and the character as two
     * separate parameters. Will replace all selections with a single, empty selection at the given position.
     * The supported options are the same as for setSelection.
     */
    void setCursor(Pos pos);

    /**
     * Set the cursor position. You can either pass a single {line, ch} object, or the line and the character as two
     * separate parameters. Will replace all selections with a single, empty selection at the given position.
     * The supported options are the same as for setSelection.
     */
    void setCursor(Pos pos, SelectionOptions options);

    /**
     * Set the cursor position. You can either pass a single {line, ch} object, or the line and the character as two
     * separate parameters. Will replace all selections with a single, empty selection at the given position.
     * The supported options are the same as for setSelection.
     */
    void setCursor(int line, int ch);


    /**
     * Set a single selection range. anchor and head should be {line, ch} objects. head defaults to anchor when not given.
     */
    void setSelection(Pos anchor, Pos head, SelectionOptions options);


    /**
     * Set a single selection range. anchor and head should be {line, ch} objects. head defaults to anchor when not given.
     */
    void setSelection(Pos anchor, Pos head);



    /**
     * Undo one edit (if any undo events are stored).
     */
    void undo();

    /**
     * Redo one undone edit.
     */
    void redo();

    /**
     * Undo one edit or selection change.
     */
    void undoSelection();

    /**
     * Redo one undone edit or selection change.
     */
    void redoSelection();

    /**
     * Clears the editor's undo history.
     */
    void clearHistory();


    TextMarker markText(Pos from, Pos to, MarkOptions options);


}
