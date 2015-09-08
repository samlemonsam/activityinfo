package org.activityinfo.geoadmin.source;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class FeatureSourceCatalogTest {

    @Test
    public void testGeometry() throws IOException {
        FeatureSourceCatalog catalog = new FeatureSourceCatalog();
        ResourceId formClassId = ResourceId.valueOf("communes");
        catalog.add(formClassId, getResource("mg/communes.shp").getFile());

        FormClass formClass = catalog.getFormClass(formClassId);
        FormField geometryField = formClass.getField(ResourceId.valueOf("the_geom"));
        
        assertThat(geometryField.getType(), Matchers.instanceOf(GeoAreaType.class));

        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("id");
        queryModel.selectField("COMMUNE").as("COMMUNE");
        queryModel.selectField("the_geom").as("geometry");
        
        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
        ColumnSet set = columnSetBuilder.build(queryModel);

        ColumnView id = set.getColumnView("id");
        ColumnView name = set.getColumnView("COMMUNE");
        ColumnView geometry = set.getColumnView("geometry");
        
        assertThat(name.getString(0), equalTo("1er Arrondissement"));
        assertThat(name.getString(1), equalTo("2e Arrondissement"));
        
        System.out.println(geometry.get(0));
    }
}