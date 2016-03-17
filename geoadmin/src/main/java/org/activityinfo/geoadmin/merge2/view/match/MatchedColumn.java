package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.activityinfo.geoadmin.match.MatchLevel;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

import java.awt.*;


/**
 * Displays a column from the source or target collection that is matched to the opposing collection
 */
public class MatchedColumn extends MatchTableColumn {

    public static final Color WARNING_COLOR = Color.decode("#FF6600");
    private final MatchTable matching;
    private final FieldProfile sourceField;
    private final FieldProfile targetField;
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
    private final MatchSide side;

    public MatchedColumn(MatchTable matching, FieldProfile targetField, FieldProfile sourceField, MatchSide side) {
        this.matching = matching;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.side = side;
    }

    @Override
    public String getHeader() {
        if(side == MatchSide.SOURCE) {
            return sourceField.getLabel();
        } else {
            return targetField.getLabel();
        }
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        if(side == MatchSide.SOURCE) {
            return formatValue(getSourceValue(rowIndex));
        } else {
            return formatValue(getTargetValue(rowIndex));
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private String getTargetValue(int rowIndex) {
        int targetRow = matching.get(rowIndex).getTargetRow();
        if(targetRow == -1) {
            return null;
        }
        return targetField.getView().getString(targetRow);
    }

    private String getSourceValue(int rowIndex) {
        int sourceRow = matching.get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        return sourceField.getView().getString(sourceRow);
    }

    public Optional<MatchLevel> getMatchConfidence(int rowIndex) {
        String sourceValue = getSourceValue(rowIndex);
        String targetValue = getTargetValue(rowIndex);

        if(Strings.isNullOrEmpty(sourceValue) && Strings.isNullOrEmpty(targetValue)) {
            return Optional.absent();

        } else if(Strings.isNullOrEmpty(sourceValue) || Strings.isNullOrEmpty(targetValue)) {
            return Optional.of(MatchLevel.POOR);
        
        } else {
            double score = scorer.score(sourceValue, targetValue);
            return Optional.of(MatchLevel.of(score));            
        }
    }

    @Override
    public Optional<MatchSide> getSide() {
        return Optional.of(side);
    }
}
