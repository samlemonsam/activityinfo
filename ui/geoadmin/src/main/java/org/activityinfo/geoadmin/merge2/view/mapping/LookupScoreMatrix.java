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

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

/**
 * Calculates distance between source lookup keys (rows) and target form instances (columns)
 */
public class LookupScoreMatrix extends ScoreMatrix {
    
    private final SourceKeySet sourceKeySet;
    private final FormProfile targetForm;
    private final int dimCount;
    
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();


    public LookupScoreMatrix(SourceKeySet sourceKeySet, FormProfile targetForm) {
        this.sourceKeySet = sourceKeySet;
        this.targetForm = targetForm;
        dimCount = sourceKeySet.getSourceFields().size();
    }
    
    private String getSourceValue(int rowIndex, int dimensionIndex) {
        return sourceKeySet.distinct().get(rowIndex).get(dimensionIndex);
    }
    
    private String getTargetValue(int columnIndex, int dimensionIndex) {
        return sourceKeySet.getTargetFields().get(dimensionIndex).getView().getString(columnIndex);
    }

    @Override
    public int getDimensionCount() {
        return dimCount;
    }

    @Override
    public int getRowCount() {
        return sourceKeySet.distinct().size();
    }

    @Override
    public int getColumnCount() {
        return targetForm.getRowCount();
    }

    public boolean matches(int rowIndex, int columnIndex) {
        for(int i=0;i<dimCount;++i) {
            String source = getSourceValue(rowIndex, i);
            String target = getTargetValue(columnIndex, i);
            if(scorer.score(source, target) > 0d) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public double score(int i, int j, int d) {
        String source = getSourceValue(i, d);
        String target = getTargetValue(j, d);
        if(Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(target)) {
            return Double.NaN;
        }
        return scorer.score(source, target);
    }
}
