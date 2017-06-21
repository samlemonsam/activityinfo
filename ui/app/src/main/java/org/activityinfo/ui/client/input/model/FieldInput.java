package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.type.FieldValue;

/**
 * Describes the user's input for a single field.
 *
 * <p>A user's input can be in one of three states:</p>
 * <ul>
 *     <li><strong>Empty</strong> - the user has entered no value for the field, or erased an existing value</li>
 *     <li><strong>Invalid</strong> - the user's current input is invalid and cannot even be interpreted as a {@link FieldValue}.
 *     For example, if the user enters a latitude of "396.40.40 Z" and a longitude of "45. 04 EE" then
 *     the input cannot be interpreted as a {@code GeoPoint} and so is considered
 *     invalid.</li>
 *     <li><strong>Valid</strong> - the user's current input can be interpreted as a {@code FieldValue}. Subsequent relevancy
 *     and validation rules may result in the value being judged as invalid done the road. </li>
 * </ul>
 */
public class FieldInput {

    public enum State {

        /**
         * The user has not entered any input for this field
         */
        UNTOUCHED,

        /**
         * The field has been set to empty by the user
         */
        EMPTY,

        /**
         * The user has entered invalid input
         */
        INVALID,

        /**
         * The user has entered a valid value
         */
        VALID;
    }

    private final State state;
    private final FieldValue fieldValue;

    /**
     * True if the field has been touched by the user in some way.
     *
     * (We generally want to hold of showing "required" errors before the user
     * has started filling out the form.)
     */
    private boolean touched;

    public static final FieldInput INVALID_INPUT = new FieldInput(State.INVALID, null);

    public static final FieldInput EMPTY = new FieldInput(State.EMPTY, null);

    public static final FieldInput UNTOUCHED = new FieldInput(State.UNTOUCHED, null);

    public FieldInput(FieldValue value) {
        if(value == null) {
            this.state = State.EMPTY;
            this.fieldValue = null;
        } else {
            this.state = State.VALID;
            this.fieldValue = value;
        }
    }

    private FieldInput(State state, FieldValue fieldValue) {
        this.state = state;
        this.fieldValue = fieldValue;
    }

    public State getState() {
        return state;
    }

    public FieldValue getValue() {
        assert state == State.VALID;
        return fieldValue;
    }

    public boolean isTouched() {
        return touched;
    }
}
