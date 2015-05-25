package org.activityinfo.geoadmin.merge2.view.model;


import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.activityinfo.geoadmin.merge.model.MatchBuilder;
import org.activityinfo.geoadmin.merge2.view.swing.merge.MergeSide;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the mappings between the <em>source</em> form and the <em>target</em> form. 
 * 
 * <p>This view is a function of the {@link org.activityinfo.geoadmin.merge2.state.MergeModel2} and 
 * the {@code FormClass}es and instances of the source and target forms.</p>
 */
public class FormMapping {
    
    private FormProfile source;
    private FormProfile target;
    private FieldMatrix distanceMatrix;
    private List<SourceFieldMapping> mappings = new ArrayList<>();
    
    public FormMapping(FormProfile source, FormProfile target) {
        this.source = source;
        this.target = target;
        this.distanceMatrix = new FieldMatrix(source.getFields(), target.getFields());

        MatchBuilder fieldGraph = new MatchBuilder(distanceMatrix);
        BiMap<FieldProfile, FieldProfile> columnMapping = fieldGraph.buildMap(target.getFields(), source.getFields());

        for (FieldProfile sourceField : source.getFields()) {
            mappings.add(new SourceFieldMapping(sourceField, Optional.fromNullable(columnMapping.inverse().get(sourceField))));
        }
    }

    public FormProfile getSource() {
        return source;
    }

    public FormProfile getTarget() {
        return target;
    }

    public List<SourceFieldMapping> getMappings() {
        return mappings;
    }
    
    public static Observable<FormMapping> compute(Observable<FormProfile> source, Observable<FormProfile> target) {
        return Observable.transform(source, target, new BiFunction<FormProfile, FormProfile, FormMapping>() {
            @Override
            public FormMapping apply(FormProfile source, FormProfile target) {
                return new FormMapping(source, target);
            }
        });
    }

    public BiMap<FieldProfile, FieldProfile> asMap(MergeSide fromSide, MergeSide toSide) {
        BiMap<FieldProfile, FieldProfile> map = HashBiMap.create();
        if(fromSide == toSide) {
            // Identity map
            for (FieldProfile fieldProfile : getFields(fromSide)) {
                map.put(fieldProfile, fieldProfile);
            }
            return  map;
        } else {
            for (SourceFieldMapping mapping : mappings) {
                if (mapping.getTargetField().isPresent()) {
                    map.put(mapping.getTargetField().get(), mapping.getSourceField());
                }
            }
            if (fromSide == MergeSide.TARGET) {
                return map;
            } else {
                return map.inverse();
            }
        }
    }

    public List<FieldProfile> getFields(MergeSide side) {
        return getProfile(side).getFields();
    }

    public FormProfile getProfile(MergeSide side) {
        if(side == MergeSide.SOURCE) {
            return source;
        } else {
            return target;
        }
    }
}
