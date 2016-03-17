package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.query.ColumnView;

import java.util.List;

/**
 * Computes the "distance" between the <em>contents</em> of two fields
 *
 */
public class FieldScoreMatrix extends ScoreMatrix {
    private List<FieldProfile> sourceColumns;
    private List<FieldProfile> targetColumns;

    private double[][] scores;
    
    public FieldScoreMatrix(Iterable<FieldProfile> sourceColumns, Iterable<FieldProfile> targetColumns) {
        this.sourceColumns = Lists.newArrayList(sourceColumns);
        this.targetColumns = Lists.newArrayList(targetColumns);
        this.scores = new double[this.sourceColumns.size()][this.targetColumns.size()];
        for (int i = 0; i < this.sourceColumns.size(); i++) {
            for (int j = 0; j < this.targetColumns.size(); j++) {
                scores[i][j] = calculateScore(i, j);
            }
        }
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
        return scores[i][j];
    }

    private double calculateScore(int i, int j) {
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

    private double scoreTextColumnMatch(FieldProfile sourceProfile, FieldProfile targetProfile) {
        ColumnView source = sourceProfile.getView();
        ColumnView target = targetProfile.getView();
        
        if(source.numRows() <= target.numRows()) {
            return scoreTextColumnMatch(source, target);
        } else {
            return scoreTextColumnMatch(target, source);
        }
    }
    
    private double scoreTextColumnMatch(ColumnView x, ColumnView y) {
        assert x.numRows() <= y.numRows();
        
        
        // For each of the rows in the smaller of the source / target sets,
        // find the score of the best match in the opposing set.
        // 
        // the score of the column pairs is then the mean of the row maxes

        
        double sumRowMaxes = 0;
        int countOfRows = 0;
        
        for (int i = 0; i < x.numRows(); i++) {
            String rowName = x.getString(i);
            if(!Strings.isNullOrEmpty(rowName)) {
                double maxScore = findScoreOfBestMatch(rowName, y);
                
                sumRowMaxes += maxScore;
                countOfRows++;
            }
        }
        
        if(countOfRows == 0) {
            return 0;
        } else {
            return sumRowMaxes / (double)countOfRows;
        }
    }

    private double findScoreOfBestMatch(String rowName, ColumnView y) {
        LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
        
        double maxScore = 0;
        for (int j = 0; j < y.numRows(); j++) {
            String colName = y.getString(j);
            if(!Strings.isNullOrEmpty(colName)) {
                double score = scorer.score(rowName, colName);
                if(score > maxScore) {
                    maxScore = score;
                }
            }
        }
        return maxScore;
    }
}
