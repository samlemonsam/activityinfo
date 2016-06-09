package org.activityinfo.store.hrd.op;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.hrd.entity.*;


public class CreateOrUpdateCollection implements Operation {
    
    private FormClass formClass;

    public CreateOrUpdateCollection(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public void execute(Datastore datastore) throws EntityNotFoundException {

        CollectionRootKey rootKey = new CollectionRootKey(formClass.getId());

        Optional<FormClassEntity> formClassEntity = datastore.loadIfPresent(rootKey.classKey());
        
        if(formClassEntity.isPresent()) {
            update(datastore, formClassEntity.get());
        } else {
            create(datastore);
        }
    }

    private void create(Datastore datastore) {
        CollectionVersionEntity versionEntity = new CollectionVersionEntity(formClass.getId());
        versionEntity.setVersion(1);
        versionEntity.setSchemaVersion(1);
        
        FormClassEntity formClassEntity = new FormClassEntity(formClass);
        formClassEntity.setSchemaVersion(1);
        
        datastore.put(versionEntity, formClassEntity);
    }

    private void update(Datastore datastore, FormClassEntity formClassEntity) throws EntityNotFoundException {

        CollectionVersionEntity versionEntity = datastore.load(new CollectionVersionKey(formClass.getId()));

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
