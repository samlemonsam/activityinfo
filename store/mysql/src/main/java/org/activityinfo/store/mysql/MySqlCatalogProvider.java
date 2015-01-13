package org.activityinfo.store.mysql;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionAccessor;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.UserDatabaseMapping;
import org.activityinfo.store.mysql.collections.UserMapping;
import org.activityinfo.store.mysql.mapping.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.util.List;


public class MySqlCatalogProvider {

    private List<MappingProvider> mappings = Lists.newArrayList();

    public MySqlCatalogProvider() {
        mappings.add(new UserDatabaseMapping());
        mappings.add(new UserMapping());
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new CollectionCatalog() {
            @Override
            public CollectionAccessor getCollection(ResourceId resourceId) {
                return new MySqlCollectionAccessor(getMapping(resourceId), executor);
            }

            public TableMapping getMapping(ResourceId resourceId) {
                for(MappingProvider mapping : mappings) {
                    if(mapping.accept(resourceId)) {
                        return mapping.getMapping(executor, resourceId);
                    }
                }
                throw new IllegalArgumentException("no such collection: " + resourceId);
            }

            @Override
            public FormClass getFormClass(ResourceId formClassId) {
                TableMapping mapping = getMapping(formClassId);
                FormClass formClass = mapping.getFormClass();

                // sanity check
                if(!formClass.getId().equals(formClassId)) {
                    throw new IllegalStateException("formClassId = " + formClassId + ", formClass.id = " + formClassId);
                }

                return formClass;
            }
        };
    }
}
