package org.activityinfo.geoadmin.model;

import org.activityinfo.geoadmin.merge.model.MergeForm;
import org.activityinfo.geoadmin.merge.model.MergeModel;
import org.activityinfo.geoadmin.source.FeatureSourceCatalog;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.io.IOException;

public class ActivityInfoClientTest {
    
    @Test
    public void test() throws IOException {
        
        ActivityInfoClient client = new ActivityInfoClient("http://localhost:8898/resources", "test@test.org", "testing123");

        ResourceId targetId = CuidAdapter.adminLevelFormClass(1508);
        MergeForm target = new MergeForm();
        target.build(client, targetId);
        
        System.out.println(target.getTextFields());


        ResourceId importId = ResourceId.generateId();
        FeatureSourceCatalog catalog = new FeatureSourceCatalog();
        catalog.add(importId, "/home/alex/dev/geodatabase/mdg_polbnda_adm2_district_NDMO_OCHA.shp");

        FormTree sourceTree = new FormTreeBuilder(catalog).queryTree(importId);


        MergeForm source = new MergeForm();
        source.build(catalog, importId);

        MergeModel model = new MergeModel(target, source);
        model.build();
        
        
    }
    
    
}