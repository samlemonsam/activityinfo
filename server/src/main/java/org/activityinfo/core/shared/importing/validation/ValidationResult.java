package org.activityinfo.core.shared.importing.validation;

import org.activityinfo.model.type.RecordRef;

import java.util.List;

public class ValidationResult {

    public enum State {
        OK, MISSING, ERROR, CONFIDENCE
    }

    public static final double MINIMUM_PERSISTENCE_SCORE = 0.5;

    public static final ValidationResult MISSING = new ValidationResult(State.MISSING) {
    };

    public static final ValidationResult OK = new ValidationResult(State.OK) {
    };

    private final State state;
    private RecordRef ref;
    private String typeConversionErrorMessage;
    private String targetValue;
    private double confidence;

    private ValidationResult(State state) {
        this.state = state;
    }

    public static ValidationResult error(String message) {
        ValidationResult result = new ValidationResult(State.ERROR);
        result.typeConversionErrorMessage = message;
        return result;
    }

    public static ValidationResult missing() {
        return new ValidationResult(State.MISSING);
    }


    public static ValidationResult converted(String targetValue, double confidence) {
        ValidationResult result = new ValidationResult(State.CONFIDENCE);
        result.targetValue = targetValue;
        result.confidence = confidence;
        return result;
    }

    public boolean hasTypeConversionError() {
        return typeConversionErrorMessage != null;
    }

    public String getTypeConversionErrorMessage() {
        return typeConversionErrorMessage;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean wasConverted() {
        return targetValue != null;
    }

    public State getState() {
        return state;
    }

    public boolean isPersistable() {
        return state == State.OK || (state == State.CONFIDENCE && confidence >= MINIMUM_PERSISTENCE_SCORE);
    }

    public boolean hasReferenceMatch() {
        return ref != null;
    }

    public RecordRef getRef() {
        return ref;
    }

    public ValidationResult setRef(RecordRef ref) {
        this.ref = ref;
        return this;
    }

    public static boolean isPersistable(List<ValidationResult> results) {
        for (ValidationResult result : results) {
            if (!result.isPersistable()) {
                return false;
            }
        }
        return true;
    }
}
