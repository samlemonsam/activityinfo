package org.activityinfo.ui.client.input.model;

import org.activityinfo.model.type.FieldValue;

/**
 * Algebraic data structure wannabe: either INVALID, EMPTY, or value.
 */
public class InputValue {

    public enum State {
        INVALID,
        EMPTY,
        VALUE
    }

    private static final InputValue EMPTY = new InputValue(State.EMPTY);

    private State state;
    private String errorMessage;
    private FieldValue value;

    private InputValue(State state) {
        this.state = state;
    }

    public static InputValue empty() {
        return EMPTY;
    }

    public static InputValue invalid(String errorMessage) {
        InputValue state = new InputValue(State.INVALID);
        state.errorMessage = errorMessage;
        return state;
    }

    public static InputValue valid(FieldValue value) {
        InputValue state = new InputValue(State.VALUE);
        state.value = value;
        return state;
    }

    public State getState() {
        return state;
    }

    public String getErrorMessage() {
        assert state == State.INVALID;
        return errorMessage;
    }

    public FieldValue getValue() {
        assert state == State.VALUE;
        return value;
    }
}
