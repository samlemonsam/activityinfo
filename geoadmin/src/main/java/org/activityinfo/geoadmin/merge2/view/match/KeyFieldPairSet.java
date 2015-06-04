package org.activityinfo.geoadmin.merge2.view.match;


import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import org.activityinfo.geoadmin.match.MatchBuilder;
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
        for (FieldProfile targetField : targetToSource.keySet()) {
            pairs.add(new KeyFieldPair(targetToSource.get(targetField), targetField));
        }
        this.pairs = pairs.build();
    }

    /**
     * Constructs a {@code KeyFieldPairSet} from a source and target form by matching columns with similar 
     * sets of values.
     */
    public static KeyFieldPairSet matchKeys(FormProfile source, FormProfile target) {

        ScoreMatrix scoreMatrix = new FieldScoreMatrix(source.getFields(), target.getFields());
        MatchBuilder fieldGraph = new MatchBuilder(scoreMatrix);

        BiMap<FieldProfile, FieldProfile> map = fieldGraph.buildMap(source.getFields(), target.getFields());
        
        return new KeyFieldPairSet(source, target, map);
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
}
