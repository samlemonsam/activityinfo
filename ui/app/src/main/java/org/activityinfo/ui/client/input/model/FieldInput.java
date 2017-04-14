package org.activityinfo.ui.client.input.model;


import org.activityinfo.model.type.FieldValue;

public class FieldInput {

    public enum State {
        EMPTY,
        INVALID,
        VALID;
    }

    private final State state;
    private final FieldValue fieldValue;

    public static final FieldInput INVALID_INPUT = new FieldInput(State.INVALID, null);
    public static final FieldInput EMPTY = new FieldInput(State.EMPTY, null);

    public FieldInput(FieldValue value) {
        assert value != null;
        this.state = State.VALID;
        this.fieldValue = value;
    }

    private FieldInput(State state, FieldValue fieldValue) {
        this.state = state;
        this.fieldValue = fieldValue;
    }

    public State getState() {
        return state;
    }

    public FieldValue getValue() {
        return fieldValue;
    }
}
