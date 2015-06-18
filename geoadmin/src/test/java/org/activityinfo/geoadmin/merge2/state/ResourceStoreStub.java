package org.activityinfo.geoadmin.merge2.state;

import org.activityinfo.geoadmin.source.FeatureSourceCatalog;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ConstantObservable;
import org.activityinfo.observable.Observable;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.ResourceStore;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import java.io.IOException;
import java.net.URL;

import static com.google.common.io.Resources.getResource;

public class ResourceStoreStub implements ResourceStore {

    public static final ResourceId REGION_TARGET_ID = ResourceId.valueOf("E0000001379");
    public static final ResourceId COMMUNE_TARGET_ID = ResourceId.valueOf("E0000001511");
    
    public static final ResourceId COMMUNE_SOURCE_ID = ResourceId.valueOf("/mg/communes.shp");
    public static final ResourceId GADM_PROVINCE_SOURCE_ID = ResourceId.valueOf("/mg/MDG_adm2.shp");


    private final FeatureSourceCatalog featureSourceCatalog;
    private final CollectionCatalogStub testCatalog; 
    private final MergedCatalog catalog = new MergedCatalog();

    public ResourceStoreStub() throws IOException {
        featureSourceCatalog = new FeatureSourceCatalog();
        addShapefile(COMMUNE_SOURCE_ID);
        addShapefile(GADM_PROVINCE_SOURCE_ID);
        
        // Madagascar Admin Levels
        testCatalog = new CollectionCatalogStub();
        testCatalog.addJsonCollection("adminLevel/E0000001379");
        testCatalog.addJsonCollection("adminLevel/E0000001508");
        testCatalog.addJsonCollection("adminLevel/E0000001511");
    }

    private void addShapefile(ResourceId resourceId) throws IOException {
        URL resource = getResource(resourceId.asString().substring(1));
        featureSourceCatalog.add(resourceId, resource.getFile());
    }

    @Override
    public Observable<FormTree> getFormTree(ResourceId resourceId) {
        FormTreeBuilder builder = new FormTreeBuilder(catalog);
        return new ConstantObservable<>(builder.queryTree(resourceId));
    }

    @Override
    public Observable<ColumnSet> queryColumns(QueryModel queryModel) {

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
        return new ConstantObservable<>(columnSetBuilder.build(queryModel));
    }
    
    private class MergedCatalog implements CollectionCatalog {

        @Override
        public ResourceCollection getCollection(ResourceId resourceId) {
            if (testCatalog.contains(resourceId)) {
                return testCatalog.getCollection(resourceId);
            } else {
                return featureSourceCatalog.getCollection(resourceId);
            }
        }

        @Override
        public Resource getResource(ResourceId resourceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FormClass getFormClass(ResourceId resourceId) {
            return getCollection(resourceId).getFormClass();
        }
    }

}
