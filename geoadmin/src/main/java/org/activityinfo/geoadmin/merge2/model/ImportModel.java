package org.activityinfo.geoadmin.merge2.model;

import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.StatefulSet;
import org.activityinfo.observable.StatefulValue;


/**
 * Models the process of importing a <em>source</em> collection of resources into an existing 
 * <em>target</em> collection.
 * 
 * <p>The import process is essentially one of reshaping semi-structured data into a well-defined
 * schema, or form class as we call it.</p>
 *
 * <p>Throughout, we refer to the data to be imported as the <em>source</em> form or collection, which 
 * serves as source of changes to a <em>target</em> collection.</p>
 * 
 * <p>For example, when updating an existing collection/form of Afghan provinces with a shapefile containing
 * higher resolution boundaries and more accurate province names, the shapefile would be the <em>source</em>
 * form and the existing Afghan provinces administrative level would be the <em>target</em> form.</p>
 * 
 */
public class ImportModel {

    private StatefulValue<ResourceId> sourceFormId = new StatefulValue<>();
    private StatefulValue<ResourceId> targetFormId = new StatefulValue<>();
    private InstanceMatchSet instanceMatchSet = new InstanceMatchSet();
    private StatefulSet<ReferenceMatch> referenceMatches = new StatefulSet<>();


    public ImportModel(ResourceId source, ResourceId target) {
        this.sourceFormId.updateValue(source);
        this.targetFormId.updateValue(target);
    }


    /**
     * 
     * @return the identity of the form to be imported.
     */
    public StatefulValue<ResourceId> getSourceFormId() {
        return sourceFormId;
    }
    

    /**
     * @return the {@code ResourceId} of the form into which we are importing new data
     */
    public Observable<ResourceId> getTargetFormId() {
        return targetFormId;
    }

    public InstanceMatchSet getInstanceMatchSet() {
        return instanceMatchSet;
    }
    
    public StatefulSet<ReferenceMatch> getReferenceMatches() {
        return referenceMatches;
    }
}
