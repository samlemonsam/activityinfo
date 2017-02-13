package org.activityinfo.ui.codemirror.client;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public interface CodeMirrorEditor {

    /**
     * Retrieve the currently active document from an editor.
     */
    CodeMirrorDocument getDoc();


    /**
     * Give the editor focus.
     */
    void focus();


    /**
     * Tells you whether the editor currently has focus.
     */
    boolean hasFocus();


    /**
     * If your code does something to change the size of the editor element
     * (window resizes are already listened for), or unhides it, you should probably follow up by calling this
     * method to ensure CodeMirror is still looking as intended. See also the autorefresh addon.
     */
    void refresh();


    /**
     * Given an {left, top} object, returns the {line, ch} position that corresponds to it.
     * The optional mode parameter determines relative to what the coordinates are interpreted.
     * It may be "window", "page" (the default), or "local".
     * @param object
     * @param mode
     * @return
     */
    Pos coordsChar(LeftTop object, String mode);

    /**
     * Given an {left, top} object, returns the {line, ch} position that corresponds to it.
     * The optional mode parameter determines relative to what the coordinates are interpreted.
     * It may be "window", "page" (the default), or "local".
     * @param object
     * @param mode
     * @return
     */
    Pos coordsChar(LeftTop object);


    /**
     * This is similar to getTokenAt, but collects all tokens for a given line into an array.
     * It is much cheaper than repeatedly calling getTokenAt, which re-parses the part of the line
     * before the token for every call.
     */
    Token[] getLineTokens(int line, boolean precise);

    /**
     * This is a (much) cheaper version of getTokenAt useful for when you just need the type of the token at a
     * given position, and no other information. Will return null for unstyled tokens, and a string,
     * potentially containing multiple space-separated style names, otherwise.
     */
    String getTokenTypeAt(Pos pos);

    /**
     * Retrieves information about the token the current mode found before the given position (a {line, ch} object).
     */
    Token getTokenAt(Pos pos);

}
