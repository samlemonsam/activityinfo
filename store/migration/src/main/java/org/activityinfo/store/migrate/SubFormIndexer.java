package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormSchemaEntity;
import org.activityinfo.store.hrd.entity.ResourceEntity;

public class SubFormIndexer extends MapOnlyMapper<Entity, Void> {

    static {
        LocaleProxy.initialize();
    }

    @Override
    public void map(Entity value) {

        ObjectifyService.run(new VoidWork() {
            @Override
            public void vrun() {
                FormSchemaEntity schemaEntity = Hrd.ofy().load().fromEntity(value);
                if(schemaEntity.getFormId().getDomain() != 'a') {
                    FormClass formClass = schemaEntity.readFormClass();
                    if (formClass.isSubForm()) {
                        ResourceEntity resource = new ResourceEntity();
                        resource.setId(formClass.getId());
                        resource.setResourceType(ResourceType.FORM);
                        resource.setParentId(formClass.getParentFormId().or(formClass.getDatabaseId()));
                        resource.setDatabaseId(formClass.getDatabaseId());
                        resource.setLabel(formClass.getLabel());

                        Hrd.ofy().save().entity(resource).now();
                    }
                }
            }
        });

    }
}
