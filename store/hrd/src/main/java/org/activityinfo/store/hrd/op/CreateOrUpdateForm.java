package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.hrd.entity.*;


public class CreateOrUpdateForm implements Operation {
    
    private FormClass formClass;

    public CreateOrUpdateForm(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public void execute(Datastore datastore) throws EntityNotFoundException {

        FormRootKey rootKey = new FormRootKey(formClass.getId());

        Optional<FormSchemaEntity> formClassEntity = datastore.loadIfPresent(rootKey.classKey());
        
        if(formClassEntity.isPresent()) {
            update(datastore, formClassEntity.get());
        } else {
            create(datastore);
        }
    }

    private void create(Datastore datastore) {
        FormVersionEntity versionEntity = new FormVersionEntity(formClass.getId());
        versionEntity.setVersion(1);
        versionEntity.setSchemaVersion(1);
        
        FormSchemaEntity formClassEntity = new FormSchemaEntity(formClass);
        formClassEntity.setSchemaVersion(1);
        
        datastore.put(versionEntity, formClassEntity);
    }

    private void update(Datastore datastore, FormSchemaEntity formClassEntity) throws EntityNotFoundException {

        FormVersionEntity versionEntity = datastore.load(new FormVersionKey(formClass.getId()));

        // Increment the version counter
        long newVersion = versionEntity.getVersion() + 1;
        versionEntity.setVersion(newVersion);
        versionEntity.setSchemaVersion(newVersion);
        
        // Update the schema
        formClassEntity.update(formClass);
        formClassEntity.setSchemaVersion(newVersion);
        
        datastore.put(versionEntity, formClassEntity);
    }
}
