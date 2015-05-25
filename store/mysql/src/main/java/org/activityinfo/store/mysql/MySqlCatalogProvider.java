package org.activityinfo.store.mysql;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.*;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.SQLException;
import java.util.List;


public class MySqlCatalogProvider {

    private List<CollectionProvider> mappings = Lists.newArrayList();

    public MySqlCatalogProvider() {
        mappings.add(new SimpleTableCollectionProvider(new DatabaseCollection()));
        mappings.add(new SimpleTableCollectionProvider(new UserCollection()));
        mappings.add(new SimpleTableCollectionProvider(new CountryCollection()));
        mappings.add(new SimpleTableCollectionProvider(new AdminCollectionProvider()));
        mappings.add(new SimpleTableCollectionProvider(new LocationCollectionProvider()));
        mappings.add(new SimpleTableCollectionProvider(new PartnerCollectionProvider()));
        mappings.add(new SiteCollectionProvider());
    }

    public CollectionCatalog openCatalog(final QueryExecutor executor) {
        return new CollectionCatalog() {
            @Override
            public ResourceCollection getCollection(ResourceId resourceId) {
                for(CollectionProvider mapping : mappings) {
                    if(mapping.accept(resourceId)) {
                        try {
                            return mapping.getAccessor(executor, resourceId);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                throw new IllegalArgumentException("no such collection: " + resourceId);
            }

            @Override
            public FormClass getFormClass(ResourceId formClassId) {
                return getCollection(formClassId).getFormClass();
            }
        };
    }
}
