package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

/**
 * Pair of key fields, one from the source form, and one from the target field, which 
 * are used to match instances between the source and target collections.
 */
public class KeyFieldPair {
    
    private FieldProfile sourceField;
    private FieldProfile targetField;
    
    private LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

    public KeyFieldPair(FieldProfile sourceField, FieldProfile targetField) {
        this.sourceField = sourceField;
        this.targetField = targetField;
    }

    public FieldProfile getTargetField() {
        return targetField;
    }

    public FieldProfile getSourceField() {
        return sourceField;
    }

    /**
     * Score the similarity between a source and a target instance based on this
     * key field pair.
     * @param sourceIndex the index within our list of source instances
     * @param targetIndex the index within our list of target instances
     * @return a score of similarity in the range [0, 1] where 1 is an exact match.              
     */
    public double score(int sourceIndex, int targetIndex) {
        String sourceValue = sourceField.getView().getString(sourceIndex);
        String targetValue = targetField.getView().getString(targetIndex);
        if (Strings.isNullOrEmpty(sourceValue) || Strings.isNullOrEmpty(targetValue)) {
            return Double.NaN;
        } else {
            return scorer.score(sourceValue, targetValue);
        }
    }
}
