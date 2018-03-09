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
package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.activityinfo.geoadmin.match.MatchLevel;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;

import java.util.Collection;
import java.util.List;

/**
 * Graph that models "closeness" between a set of source keys and 
 * instances of a target reference field.
 * 
 */
public class LookupGraph {

    private final SourceKeySet sourceKeySet;
    private final FormProfile targetForm;
    private final LookupScoreMatrix matrix;
    private final Multimap<Integer, Integer> candidates = HashMultimap.create();
    private final int optimalMatches[];


    public LookupGraph(SourceKeySet sourceKeySet, FormProfile targetForm) {
        this.sourceKeySet = sourceKeySet;
        this.targetForm = targetForm;
        this.matrix = new LookupScoreMatrix(sourceKeySet, targetForm);
        
        for(int i=0;i!=sourceKeySet.distinct().size();++i) {
            for(int j=0;j<targetForm.getRowCount();++j) {
                if(matrix.matches(i, j)) {
                    candidates.put(i, j);
                }
            }
        }
        
        optimalMatches = matchParetoOptimal();
    }

    public SourceKeySet getSourceKeySet() {
        return sourceKeySet;
    }

    public int getParetoOptimalMatch(int sourceKeyIndex) {
        return optimalMatches[sourceKeyIndex];
    }
    
    public List<FieldProfile> getTargetKeyFields() {
        return sourceKeySet.getTargetFields();
    }

    public int[] matchParetoOptimal() {
        int matches[] = new int[sourceKeySet.size()];
        for(int sourceKeyIndex=0;sourceKeyIndex!=sourceKeySet.distinct().size();++sourceKeyIndex) {
            Collection<Integer> targetRows = candidates.get(sourceKeyIndex);
            matches[sourceKeyIndex] = -1;
            if(!targetRows.isEmpty()) {
                int bestMatch = bestMatch(sourceKeyIndex, targetRows);
                if(isParetoOptimal(sourceKeyIndex, bestMatch, targetRows)) {
                    matches[sourceKeyIndex] = bestMatch;                                
                }
            }
        }
        return matches;
    }
    

    /**
     * Finds the "best" match among potential candidates, using the sum of the scores across
     * the dimensions as a criteria.
     *
     * @return the index of the target instance that best matches the given source key
     */
    private int bestMatch(int sourceKeyIndex, Collection<Integer> targetIndexes) {
        
        double bestScore = 0;
        int bestMatch = -1;
        for(Integer targetIndex : targetIndexes) {
            double score = matrix.sumScores(sourceKeyIndex, targetIndex);
            if(score > bestScore) {
                bestScore = score;
                bestMatch = targetIndex;
            }
        }
        return bestMatch;
    }

    private boolean isParetoOptimal(int sourceKeyIndex, int bestTargetIndex, Collection<Integer> targetCandidates) {

        double scores[] = matrix.score(sourceKeyIndex, bestTargetIndex);

        for (Integer targetIndex : targetCandidates) {
            if( targetIndex != bestTargetIndex &&
                    !dominates(scores, matrix.score(sourceKeyIndex, targetIndex))) {

                return false;
            }
        }

        return true;
    }


    /**
     * @return true if {@code x[i] > y[i]} for all {@code i}
     */
    private boolean dominates(double[] x, double[] y) {
        assert x.length == y.length;
        for (int i = 0; i < x.length; i++) {
            if(y[i] > x[i]) {
                return false;
            }
        }
        return true;
    }

    public MatchLevel getLookupConfidence(int keyIndex, int targetIndex) {
        if(targetIndex == -1) {
            return MatchLevel.POOR;
        }
        double minScore = matrix.getMinScore(keyIndex, targetIndex);
        return MatchLevel.of(minScore);
    }

    public Collection<Integer> getCandidates(int sourceKeyIndex) {
        return candidates.get(sourceKeyIndex);
    }

    public double getScore(int sourceKeyIndex, Integer targetRow) {
        return matrix.sumScores(sourceKeyIndex, targetRow);
    }

    public FormProfile getTargetForm() {
        return targetForm;
    }
}
