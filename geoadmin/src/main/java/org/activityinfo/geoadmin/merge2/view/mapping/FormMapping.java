package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.ResourceStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the mapping of fields from the source field to the target field.
 */
public class FormMapping {

    private final List<FieldMapping> fieldMappings = new ArrayList<>();
    private final List<ReferenceFieldMapping> referenceFieldMappings = new ArrayList<>();

    public FormMapping(List<FieldMapping> fieldMappings) {
        this.fieldMappings.addAll(fieldMappings);
        for (FieldMapping fieldMapping : fieldMappings) {
            if(fieldMapping instanceof ReferenceFieldMapping) {
                referenceFieldMappings.add((ReferenceFieldMapping) fieldMapping);
            }
        }
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public List<ReferenceFieldMapping> getReferenceFieldMappings() {
        return referenceFieldMappings;
    }

    public static Observable<FormMapping> computeFromMatching(final ResourceStore store, final Observable<KeyFieldPairSet> matching) {
        return matching.join(new Function<KeyFieldPairSet, Observable<FormMapping>>() {
            @Override
            public Observable<FormMapping> apply(KeyFieldPairSet input) {
                FormMappingBuilder builder = new FormMappingBuilder(store, input);
                return builder.build().transform(new Function<List<FieldMapping>, FormMapping>() {
                    @Override
                    public FormMapping apply(List<FieldMapping> input) {
                        return new FormMapping(input);
                    }
                });
            }
        });
    }

}
