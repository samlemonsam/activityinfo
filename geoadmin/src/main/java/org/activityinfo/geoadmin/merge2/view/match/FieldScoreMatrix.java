package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;

import java.util.List;
import java.util.Set;

/**
 * Computes the "distance" between the <em>contents</em> of two fields
 *
 */
public class FieldScoreMatrix extends ScoreMatrix {
    private List<FieldProfile> sourceColumns;
    private List<FieldProfile> targetColumns;

    public FieldScoreMatrix(Iterable<FieldProfile> sourceColumns, Iterable<FieldProfile> targetColumns) {
        this.sourceColumns = Lists.newArrayList(sourceColumns);
        this.targetColumns = Lists.newArrayList(targetColumns);
    }

    @Override
    public int getDimensionCount() {
        return 1;
    }


    @Override
    public int getRowCount() {
        return sourceColumns.size();
    }

    @Override
    public int getColumnCount() {
        return targetColumns.size();
    }

    @Override
    public double score(int i, int j, int d) {
        FieldProfile x = sourceColumns.get(i);
        FieldProfile y = targetColumns.get(j);

        if(x.isText() && y.isText()) {
            return scoreTextColumnMatch(x, y);

        } else if(x.isGeoArea() && y.isGeoArea()) {
            return scoreGeoAreaMatch(x, y);
        
        } else {
            return 0;
        }
    }

    private double scoreGeoAreaMatch(FieldProfile x, FieldProfile y) {
        // TODO: only really needed if we have forms with multiple geo area fields.
        return 1.0;
    }

    private double scoreTextColumnMatch(FieldProfile x, FieldProfile y) {
        Set<String> sourceValues = x.uniqueValues();
        Set<String> targetValues = y.uniqueValues();

        // Alternative: Relative information
        // I(s; p) = sum(i = 1 to n)  s[i] * log(s[i]/p[i])

        double intersection = Sets.intersection(targetValues, sourceValues).size();
        if (intersection > 0) {
            double union = Sets.union(targetValues, sourceValues).size();
            return intersection / union;

        } else {
            return 0d;
        }
    }
}
