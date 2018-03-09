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

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulSet;
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

    public static Observable<FormMapping> computeFromMatching(final ResourceStore store,
                                                              final Observable<KeyFieldPairSet> matching, 
                                                              final StatefulSet<ReferenceMatch> referenceMatches) {
        return matching.join(new Function<KeyFieldPairSet, Observable<FormMapping>>() {
            @Override
            public Observable<FormMapping> apply(KeyFieldPairSet input) {
                FormMappingBuilder builder = new FormMappingBuilder(store, input, referenceMatches);
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
