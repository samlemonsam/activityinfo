package org.activityinfo.store.hrd.op;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.hrd.entity.FormEntity;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class CreateOrUpdateForm extends VoidWork {
    
    private FormClass formClass;

    public CreateOrUpdateForm(FormClass formClass) {
        this.formClass = formClass;
    }
    
    @Override
    public void vrun() {

        Key<FormSchemaEntity> schemaKey = FormSchemaEntity.key(formClass.getId());
        FormSchemaEntity schemaEntity = ofy().load().key(schemaKey).now();
        
        if(schemaEntity == null) {
            create();
        } else {
            update(schemaEntity);
        }
    }

    private void create() {
        FormEntity rootEntity = new FormEntity();
        rootEntity.setId(formClass.getId());
        rootEntity.setVersion(1);
        rootEntity.setSchemaVersion(1);
        
        FormSchemaEntity formClassEntity = new FormSchemaEntity(formClass);
        formClassEntity.setSchemaVersion(1);
        
        ofy().save().entities(rootEntity, formClassEntity);
    }

    private void update(FormSchemaEntity formClassEntity) {

        FormEntity rootEntity = ofy().load().key(FormEntity.key(formClass)).safe();
        
        // Increment the version counter
        long newVersion = rootEntity.getVersion() + 1;
        rootEntity.setVersion(newVersion);
        rootEntity.setSchemaVersion(newVersion);
        
        // Update the schema
        formClassEntity.setSchema(formClass);
        formClassEntity.setSchemaVersion(newVersion);
        
        ofy().save().entities(rootEntity, formClassEntity);
    }
}
