package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SynchronousScheduler;
import org.activityinfo.store.ResourceStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs a set of {@link org.activityinfo.geoadmin.merge2.view.mapping.FieldMapping}s
 * 
 */
public class FormMappingBuilder {
    
    private final ResourceStore resourceStore;
    private final FormProfile source;
    private final FormProfile target;
    
    private final KeyFieldPairSet fieldMatching;

    private List<Observable<FieldMapping>> mappings = new ArrayList<>();

    public FormMappingBuilder(ResourceStore resourceStore,
                              KeyFieldPairSet fieldMatching) {
        this.resourceStore = resourceStore;
        this.source = fieldMatching.getSource();
        this.target = fieldMatching.getTarget();
        this.fieldMatching = fieldMatching;
    }
    
    public Observable<List<FieldMapping>> build() {
        // Add simple field mappings, where we just copy the text/quantity/etc field from 
        // the source to the target field
        for (FieldProfile targetField : target.getFields()) {
            if(SimpleFieldMapping.isSimple(targetField)) {
                buildSimpleMapping(targetField);
            }
        }
        
        // Add mappings for ReferenceFields, for which we have to perform look ups
        for (FormTree.Node targetNode : target.getFormTree().getRootFields()) {
            if(targetNode.isReference()) {
                buildReferenceMapping(targetNode.getField());
            }
        }
        
        return Observable.flatten(SynchronousScheduler.INSTANCE, mappings);
    }
    


    /**
     * Builds a reference mapping for a given target node.
     * 
     * <p>A reference field takes the value of a single {@code ResourceId}, but
     * most of the time we don't have the actual id of the field in the dataset to import:
     * we have to obtain the id by performing a look up against text fields in the 
     * </p>
     *
     * @param targetField the reference field to look up
     */
    private void buildReferenceMapping(final FormField targetField) {

        // In order to match against the ReferenceField, we need the actual data
        // from the form that is being referenced. 
        // 
        // Example: If we have a "Location" field that references the "Province" form class,
        // then we need then names and/or codes of the Province form class in order to lookup the
        // ids, assuming that our source dataset has a "province name" column with the names of the provinces.

        ReferenceType type = (ReferenceType) targetField.getType();

        // Currently this only supports reference fields that reference exactly one form class.
        if (type.getCardinality() == Cardinality.SINGLE && type.getRange().size() == 1) {

            ResourceId referenceFormId = Iterables.getOnlyElement(type.getRange());
            Observable<FormProfile> targetReferencedFormProfile = FormProfile.profile(resourceStore, referenceFormId);

            Observable<FieldMapping> mapping = targetReferencedFormProfile.transform(new Function<FormProfile, FieldMapping>() {
                @Override
                public FieldMapping apply(FormProfile target) {
                    return new ReferenceFieldMapping(targetField, KeyFieldPairSet.matchKeys(source, target));
                }
            });
            mappings.add(mapping);
        }
    }


    private void buildSimpleMapping(FieldProfile targetField) {
        Optional<FieldProfile> sourceField = fieldMatching.targetToSource(targetField);
        if(sourceField.isPresent()) {
            mappings.add(Observable.<FieldMapping>just(new SimpleFieldMapping(sourceField.get(), targetField)));
        }
    }

}
