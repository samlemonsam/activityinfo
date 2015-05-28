package org.activityinfo.geoadmin.merge2.view.match;


import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import org.activityinfo.geoadmin.match.DistanceMatrix;
import org.activityinfo.geoadmin.merge.model.MatchBuilder;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;

/**
 * Describes the mappings between the <em>source</em> form and the <em>target</em> form. 
 * 
 * <p>This view is a function of the {@link org.activityinfo.geoadmin.merge2.model.ImportModel} and 
 * the {@code FormClass}es and instances of the source and target forms.</p>
 */
public class FormMapping {
    
    private FormProfile source;
    private FormProfile target;
    private final BiMap<FieldProfile, FieldProfile> targetToSource;

    public FormMapping(FormProfile source, FormProfile target) {
        this.source = source;
        this.target = target;

        DistanceMatrix distanceMatrix = new FieldDistanceMatrix(source.getFields(), target.getFields());
        MatchBuilder fieldGraph = new MatchBuilder(distanceMatrix);
        
        targetToSource = fieldGraph.buildMap(source.getFields(), target.getFields());
    }

    public FormProfile getSource() {
        return source;
    }

    public FormProfile getTarget() {
        return target;
    }

    
    public static Observable<FormMapping> compute(Observable<FormProfile> source, Observable<FormProfile> target) {
        return Observable.transform(source, target, new BiFunction<FormProfile, FormProfile, FormMapping>() {
            @Override
            public FormMapping apply(FormProfile source, FormProfile target) {
                return new FormMapping(source, target);
            }
        });
    }

    public BiMap<FieldProfile, FieldProfile> asMap() {
        return targetToSource;
    }
    
    public Optional<FieldProfile> sourceToTarget(FieldProfile source) {
        return Optional.fromNullable(targetToSource.inverse().get(source));
    }
    
    public Optional<FieldProfile> targetToSource(FieldProfile target) {
        return Optional.fromNullable(targetToSource.get(target));
    }
}
