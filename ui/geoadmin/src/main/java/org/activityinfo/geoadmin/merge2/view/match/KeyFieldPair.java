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
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.model.type.geo.Extents;

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

    public FieldProfile getField(MatchSide matchSide) {
        if(matchSide == MatchSide.SOURCE) {
            return getSourceField();
        } else {
            return getTargetField();
        }
    }

    /**
     * Score the similarity between a source and a target instance based on this
     * key field pair.
     * @param sourceIndex the index within our list of source instances
     * @param targetIndex the index within our list of target instances
     * @return a score of similarity in the range [0, 1] where 1 is an exact match.              
     */
    public double score(int sourceIndex, int targetIndex) {
        if(isTextPair()) {
            String sourceValue = sourceField.getView().getString(sourceIndex);
            String targetValue = targetField.getView().getString(targetIndex);
            if (Strings.isNullOrEmpty(sourceValue) || Strings.isNullOrEmpty(targetValue)) {
                return Double.NaN;
            } else {
                return scorer.score(sourceValue, targetValue);
            }
        } else if(isGeoPair()) {
            Extents sourceExtents = sourceField.getExtents(sourceIndex);
            if(sourceExtents == null) {
                return Double.NaN;
            }
            Extents targetExtents = targetField.getExtents(targetIndex);
            if(targetExtents == null) {
                return Double.NaN;
            }
            return jaccard(sourceExtents, targetExtents);
        } else {
            return Double.NaN;
        }
    }

    public boolean isGeoPair() {
        return sourceField.isGeoArea() && targetField.isGeoArea();
    }

    public boolean isTextPair() {
        return sourceField.isText() && targetField.isText();
    }

    public static double jaccard(Extents a, Extents b) {
        // https://en.wikipedia.org/wiki/Jaccard_index
        double areaA = a.area();
        double areaB = b.area();
        double areaIntersection = a.intersect(b).area();
        double jaccardIndex = areaIntersection / (a.area() + b.area() - areaIntersection);

        return jaccardIndex;
    }
}
