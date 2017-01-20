package org.activityinfo.geoadmin.merge2.state;

import com.google.common.base.Optional;
import org.activityinfo.geoadmin.source.FeatureSourceCatalog;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.ConstantObservable;
import org.activityinfo.observable.Observable;
import org.activityinfo.service.store.FormCatalog;
import org.activityinfo.service.store.FormStorage;
import org.activityinfo.store.ResourceStore;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

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


    private final FeatureSourceCatalog featureSourceCatalog;
    private final FormCatalogStub testCatalog; 
    private final MergedCatalog catalog = new MergedCatalog();

    public ResourceStoreStub() throws IOException {
        featureSourceCatalog = new FeatureSourceCatalog();
        addShapefile(COMMUNE_SOURCE_ID);
        addShapefile(GADM_PROVINCE_SOURCE_ID);
        
        // Madagascar Admin Levels
        testCatalog = new FormCatalogStub();
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

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog);
        return new ConstantObservable<>(columnSetBuilder.build(queryModel));
    }
    
    private class MergedCatalog implements FormCatalog {

        @Override
        public Optional<FormStorage> getForm(ResourceId formId) {
            if (testCatalog.contains(formId)) {
                return testCatalog.getForm(formId);
            } else {
                return featureSourceCatalog.getForm(formId);
            }
        }

        @Override
        public Optional<FormStorage> lookupForm(ResourceId recordId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CatalogEntry> getRootEntries() {
            return Collections.emptyList();
        }

        @Override
        public List<CatalogEntry> getChildren(String parentId, int userId) {
            return Collections.emptyList();
        }


        @Override
        public FormClass getFormClass(ResourceId resourceId) {
            return getForm(resourceId).get().getFormClass();
        }
    }

}
