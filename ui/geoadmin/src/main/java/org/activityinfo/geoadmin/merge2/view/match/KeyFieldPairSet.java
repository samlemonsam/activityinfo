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

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;

import java.util.Iterator;
import java.util.List;

/**
 * Set of key field pairs that are used to match the instances between the source and target collections.
 *
 */
public class KeyFieldPairSet implements Iterable<KeyFieldPair> {
    
    private FormProfile source;
    private FormProfile target;
    private final BiMap<FieldProfile, FieldProfile> targetToSource;
    private ImmutableList<KeyFieldPair> pairs;

    public KeyFieldPairSet(FormProfile source, FormProfile target, BiMap<FieldProfile, FieldProfile> targetToSource) {
        this.source = source;
        this.target = target;
        this.targetToSource = targetToSource;

        ImmutableList.Builder<KeyFieldPair> pairs = ImmutableList.builder();
        for (FieldProfile targetField : target.getFields()) {
            FieldProfile sourceField = targetToSource.get(targetField);
            if(sourceField != null) {
                pairs.add(new KeyFieldPair(sourceField, targetField));
            }
        }
        this.pairs = pairs.build();
    }

    /**
     * Constructs a {@code KeyFieldPairSet} from a source and target form by matching columns with similar 
     * sets of values.
     */
    public static KeyFieldPairSet matchKeys(FormProfile source, FormProfile target) {
        ScoreMatrix scoreMatrix = new FieldScoreMatrix(source.getFields(), target.getFields());

        dumpScoreMatrix(source, target, scoreMatrix);
        
        MatchGraph matchGraph = new MatchGraph(scoreMatrix);
        matchGraph.build();
        
        BiMap<FieldProfile, FieldProfile> targetToSource = HashBiMap.create();
        for(int sourceColumnIndex=0;sourceColumnIndex!=scoreMatrix.getRowCount();++sourceColumnIndex) {
            int targetColumnIndex = matchGraph.getBestMatchForSource(sourceColumnIndex);
            if(targetColumnIndex != -1) {
                FieldProfile sourceColumn = source.getFields().get(sourceColumnIndex);
                FieldProfile targetColumn = target.getFields().get(targetColumnIndex);

                targetToSource.put(targetColumn, sourceColumn);

                System.out.println("Matching FIELD " + sourceColumn.getLabel() + " => " + targetColumn.getLabel());
            }
        }


        return new KeyFieldPairSet(source, target, targetToSource);
    }

    private static void dumpScoreMatrix(FormProfile source, FormProfile target, ScoreMatrix scoreMatrix) {
        System.out.println("===== FieldScoreMatrix ============== ");
        for (int sourceIndex = 0; sourceIndex < source.getFields().size(); sourceIndex++) {
            for (int targetIndex = 0; targetIndex < target.getFields().size(); targetIndex++) {
                System.out.println(String.format("%20s %20s %5f",
                        source.getFields().get(sourceIndex).getLabel(),
                        target.getFields().get(targetIndex).getLabel(),
                        scoreMatrix.score(sourceIndex, targetIndex, 0)));
            }   
        }
        System.out.println("===================================== ");
    }

    public int getSourceCount() {
        return source.getRowCount();
    }
    
    public int getTargetCount() {
        return target.getRowCount();
    }
    
    public FormProfile getSource() {
        return source;
    }

    public FormProfile getTarget() {
        return target;
    }
    
    public static Observable<KeyFieldPairSet> compute(Observable<FormProfile> source, Observable<FormProfile> target) {
        return Observable.transform(source, target, new BiFunction<FormProfile, FormProfile, KeyFieldPairSet>() {
            @Override
            public KeyFieldPairSet apply(FormProfile source, FormProfile target) {
                return KeyFieldPairSet.matchKeys(source, target);
            }
        });
    }

    public Optional<FieldProfile> sourceToTarget(FieldProfile source) {
        return Optional.fromNullable(targetToSource.inverse().get(source));
    }
    
    public Optional<FieldProfile> targetToSource(FieldProfile target) {
        return Optional.fromNullable(targetToSource.get(target));
    }

    @Override
    public Iterator<KeyFieldPair> iterator() {
        return pairs.iterator();
    }

    public int size() {
        return pairs.size();
    }

    public List<KeyFieldPair> asList() {
        return pairs;
    }

    public KeyFieldPair get(int i) {
        return pairs.get(i);
    }

    public FieldProfile getField(int keyIndex, MatchSide side) {
        KeyFieldPair pair = get(keyIndex);
        if(side == MatchSide.SOURCE) {
            return pair.getSourceField();
        } else {
            return pair.getTargetField();
        }
    }

    public FormProfile getForm(MatchSide side) {
        if(side == MatchSide.SOURCE) {
            return getSource();
        } else {
            return getTarget();
        }
    }
    
    public String toDebugString(int source, int target) {
        StringBuilder sb = new StringBuilder();
        for (KeyFieldPair pair : pairs) {
            sb.append("[")
                .append(pair.getSourceField().getView().get(source))
                .append(" <> ")
                .append(pair.getTargetField().getView().get(target))
                .append(" = ")
                .append(pair.score(source, target))
                .append("]");
        }
        return sb.toString();
    }
    
    public void dump() {

        System.out.println("================ PAIRLIST =============");
        for (KeyFieldPair pair : this.pairs) {
            System.out.println(pair.getSourceField().getLabel() + " => " + pair.getTargetField().getLabel());
        }
        System.out.println("=======================================");

    }
    
    public void dumpPair(int source, int target) {
        System.out.println(toDebugString(source, target));
    }
    
}
