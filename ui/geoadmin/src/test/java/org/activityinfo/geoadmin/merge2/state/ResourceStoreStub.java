package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.source.FeatureSourceStorageProvider;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ConstantObservable;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.ResourceStore;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.FormStorageProvider;
import org.activityinfo.store.spi.FormStorage;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.io.Resources.getResource;

public class ResourceStoreStub implements ResourceStore {

    public static final ResourceId REGION_TARGET_ID = ResourceId.valueOf("E0000001379");
    public static final ResourceId COMMUNE_TARGET_ID = ResourceId.valueOf("E0000001511");
    
    public static final ResourceId COMMUNE_SOURCE_ID = ResourceId.valueOf("/mg/communes.shp");
    public static final ResourceId GADM_PROVINCE_SOURCE_ID = ResourceId.valueOf("/mg/MDG_adm2.shp");


    private final FeatureSourceStorageProvider featureSourceCatalog;
    private final FormStorageProviderStub testCatalog;
    private final MergedStorageProvider catalog = new MergedStorageProvider();

    public ResourceStoreStub() throws IOException {
        featureSourceCatalog = new FeatureSourceStorageProvider();
        addShapefile(COMMUNE_SOURCE_ID);
        addShapefile(GADM_PROVINCE_SOURCE_ID);
        
        // Madagascar Admin Levels
        testCatalog = new FormStorageProviderStub();
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

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        return new ConstantObservable<>(columnSetBuilder.build(queryModel));
    }
    
    private class MergedStorageProvider implements FormStorageProvider {

        @Override
        public Optional<FormStorage> getForm(ResourceId formId) {
            if (testCatalog.contains(formId)) {
                return testCatalog.getForm(formId);
            } else {
                return featureSourceCatalog.getForm(formId);
            }
        }

        @Override
        public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
            throw new UnsupportedOperationException();
        }


        @Override
        public FormClass getFormClass(ResourceId formId) {
            return getForm(formId).get().getFormClass();
        }
    }

}
