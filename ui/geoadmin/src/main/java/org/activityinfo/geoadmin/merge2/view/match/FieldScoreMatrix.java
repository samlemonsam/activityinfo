/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.type.geo.Extents;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

        double sumRowMaxes = 0;
        int countOfRows = 0;

        for (int i = 0; i < x.getNumRows(); i++) {
            Extents rowExtents = x.getExtents(i);
            if(rowExtents != null) {
                sumRowMaxes += findScoreOfBestMatch(rowExtents, y);
                countOfRows++;
            }
        }
        if(countOfRows == 0) {
            return 0;
        } else {
            return sumRowMaxes / (double) countOfRows;
        }
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

        Cache<String, Double> cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build();

        for (Integer i : sampleRows(x)) {
            String rowName = x.getString(i);
            if(!Strings.isNullOrEmpty(rowName)) {
                Double maxScore = cache.getIfPresent(rowName);
                if (maxScore == null) {
                    maxScore = findScoreOfBestMatch(rowName, y);
                    cache.put(rowName, maxScore);
                }
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

    private Iterable<Integer> sampleRows(ColumnView x) {
        final int rowCount = x.numRows();
        if(rowCount < 100) {
            return new Iterable<Integer>() {
                @Override
                public Iterator<Integer> iterator() {
                    return new UnmodifiableIterator<Integer>() {

                        private int i=0;

                        @Override
                        public boolean hasNext() {
                            return i < rowCount;
                        }

                        @Override
                        public Integer next() {
                            return i++;
                        }
                    };
                }
            };
        } else {
            return new Iterable<Integer>() {
                @Override
                public Iterator<Integer> iterator() {
                    return new UnmodifiableIterator<Integer>() {

                        int sampled = 0;
                        Random random = new Random();

                        @Override
                        public boolean hasNext() {
                            return sampled < 100;
                        }

                        @Override
                        public Integer next() {
                            sampled++;
                            return random.nextInt(rowCount);
                        }
                    };
                }
            };
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

    private double findScoreOfBestMatch(Extents rowExtents, FieldProfile y) {
        double maxScore = 0;
        for (int j = 0; j < y.getNumRows(); j++) {
            Extents extents = y.getExtents(j);
            if(extents != null) {
                double score = KeyFieldPair.jaccard(rowExtents, extents);
                if(score > maxScore) {
                    maxScore = score;
                }
            }
        }
        return maxScore;
    }

}
