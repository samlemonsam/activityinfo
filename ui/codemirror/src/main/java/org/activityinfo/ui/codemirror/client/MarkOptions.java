package org.activityinfo.ui.codemirror.client;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsType;

@JsType
public class MarkOptions {

    /**
     * Assigns a CSS class to the marked stretch of text.
     */
    public String className;

    /**
     * Determines whether text inserted on the left of the marker will end up inside or outside of it.
     */
    public boolean inclusiveLeft;

    /**
     * Like inclusiveLeft, but for the right side.
     */
    public boolean inclusiveRight;

    /**
     * Atomic ranges act as a single unit when cursor movement is concerned—i.e. it is impossible to place
     * the cursor inside of them. In atomic ranges, inclusiveLeft and inclusiveRight have a different
     * meaning—they will prevent the cursor from being placed respectively directly before and directly after the range.
     */
    public boolean atomic;

    /**
     * When enabled, will cause the mark to clear itself whenever the cursor enters its range. This is mostly
     * useful for text-replacement widgets that need to 'snap open' when the user tries to edit them.
     * The "clear" event fired on the range handle can be used to be notified when this happens.
     */
    public boolean clearOnEnter;

    /**
     * Determines whether the mark is automatically cleared when it becomes empty. Default is true.
     */
    public boolean clearWhenEmpty;

    /**
     * Use a given node to display this range. Implies both collapsed and atomic.
     * The given DOM node must be an inline element (as opposed to a block element).
     */
    public Element replacedWith;


    /**
     * When replacedWith is given, this determines whether the editor will capture mouse and drag events occurring in
     * this widget. Default is false—the events will be left alone for the default browser handler,
     * or specific handlers on the widget, to capture.
     */
    public boolean handleMouseEvents;


    /**
     * A read-only span can, as long as it is not cleared, not be modified except by calling setValue to reset
     * the whole document. Note: adding a read-only span currently clears the undo history of the editor,
     * because existing undo events being partially nullified by read-only spans would corrupt the history
     * (in the current implementation).
     */
    public boolean readOnly;


    /**
     * When set to true (default is false), adding this marker will create an event in the undo history that
     * can be individually undone (clearing the marker).
     */
    public boolean addToHistory;

    /**
     * Can be used to specify an extra CSS class to be applied to the leftmost span that is part of the marker.
     */
    public String startStyle;


    /**
     * A string of CSS to be applied to the covered text. For example "color: #fe3".
     */
    public String css;


    /**
     * When given, will give the nodes created for this span a HTML title attribute with the given value.
     */
    public String title;


    /**
     * When the target document is linked to other documents, you can set shared to true to make the marker appear in
     * all documents. By default, a marker appears only in its target document.
     */
    public boolean shared;
}
