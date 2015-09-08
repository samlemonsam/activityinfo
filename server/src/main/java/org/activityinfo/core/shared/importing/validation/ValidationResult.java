package org.activityinfo.core.shared.importing.validation;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.core.shared.Pair;
import org.activityinfo.core.shared.importing.strategy.InstanceScorer;

public class ValidationResult {

    public static enum State {
        OK, MISSING, ERROR, CONFIDENCE
    }

    public static final ValidationResult MISSING = new ValidationResult(State.MISSING) {
    };

    public static final ValidationResult OK = new ValidationResult(State.OK) {
    };

    private final State state;
    private Pair<ResourceId, ResourceId> rangeWithInstanceId;
    private ResourceId instanceId;
    private String typeConversionErrorMessage;
    private String convertedValue;
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


    public static ValidationResult converted(String value, double confidence) {
        ValidationResult result = new ValidationResult(State.CONFIDENCE);
        result.convertedValue = value;
        result.confidence = confidence;
        return result;
    }

    public boolean hasTypeConversionError() {
        return typeConversionErrorMessage != null;
    }

    public String getTypeConversionErrorMessage() {
        return typeConversionErrorMessage;
    }

    public String getConvertedValue() {
        return convertedValue;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean wasConverted() {
        return convertedValue != null;
    }

    public State getState() {
        return state;
    }

    public boolean isPersistable() {
        return state == State.OK || (state == State.CONFIDENCE && confidence >= InstanceScorer.MINIMUM_SCORE);
    }

    public ResourceId getInstanceId() {
        return instanceId;
    }

    public ValidationResult setInstanceId(ResourceId instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public boolean hasReferenceMatch() {
        return instanceId != null;
    }

    public Pair<ResourceId, ResourceId> getRangeWithInstanceId() {
        return rangeWithInstanceId;
    }

    public void setRangeWithInstanceId(Pair<ResourceId, ResourceId> rangeWithInstanceId) {
        this.rangeWithInstanceId = rangeWithInstanceId;
    }
}
