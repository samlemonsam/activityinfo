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

import org.activityinfo.geoadmin.match.ScoreMatrix;

import java.util.List;

/**
 * Matrix representing the distance between two sets of form instances.
 */
public class InstanceMatrix extends ScoreMatrix {
    
    private static final double MIN_SCORE = 0.5;

    private int sourceCount;
    private int targetCount;
    private int dimensionCount;

    private List<KeyFieldPair> keyFields;

    public InstanceMatrix(KeyFieldPairSet keyFields) {
        dimensionCount = keyFields.size();
        this.sourceCount = keyFields.getSourceCount();
        this.targetCount = keyFields.getTargetCount();
        this.keyFields = keyFields.asList();
    }

    @Override
    public String[] getDimensionNames() {
        String[] names = new String[keyFields.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = keyFields.get(i).getTargetField().getLabel();
        }
        return names;
    }

    @Override
    public int getDimensionCount() {
        return dimensionCount;
    }

    @Override
    public int getRowCount() {
        return sourceCount;
    }

    @Override
    public int getColumnCount() {
        return targetCount;
    }

    @Override
    public double score(int sourceIndex, int targetIndex, int dimensionIndex) {
        double score = keyFields.get(dimensionIndex).score(sourceIndex, targetIndex);
        if(score > MIN_SCORE) {
            return score;
        } else {
            // Discard low scores to avoid too much noise 
            return 0.0;
        }
    }
}
