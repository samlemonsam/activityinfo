package org.activityinfo.store.migrate;

import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.util.logging.Logger;

public class ActivateColumnStorage extends Job0<Void> {

    private ResourceId formId;

    private static final Logger LOGGER = Logger.getLogger(ActivateColumnStorage.class.getName());

    public ActivateColumnStorage(ResourceId formId) {
        this.formId = formId;
    }

    @Override
    public Value<Void> run() throws Exception {

        LOGGER.info("All records assigned numbers, activating column storage");

        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {

                Hrd.ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        FormEntity formEntity = Hrd.ofy().load().key(FormEntity.key(formId)).safe();
                        if(!formEntity.isColumnStorageActive()) {
                            formEntity.setColumnStorageActive(true);
                            Hrd.ofy().save().entity(formEntity).now();
                        }
                    }
                });
            }
        });

        return new ImmediateValue<>(null);
    }
}
