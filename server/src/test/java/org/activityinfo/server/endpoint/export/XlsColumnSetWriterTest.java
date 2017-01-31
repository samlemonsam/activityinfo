package org.activityinfo.server.endpoint.export;

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.formTree.AsyncFormTreeBuilder;
import org.activityinfo.model.formTree.ColumnNode;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.xlsform.XlsColumnSetWriter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.activityinfo.promise.PromiseMatchers.assertResolves;

/**
 * Created by yuriyz on 9/2/2016.
 */
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class XlsColumnSetWriterTest extends CommandTestCase2 {

    @Test
    public void export() throws IOException {

        ResourceId rootFormClassId = CuidAdapter.activityFormClass(1);

        FormTree formTree = assertResolves(new AsyncFormTreeBuilder(locator).apply(rootFormClassId));

        QueryModel queryModel = new QueryModel(rootFormClassId);
        queryModel.selectResourceId().as("id");
        for (ColumnNode column : formTree.getColumnNodes()) {
            if (column.getNode().getType() instanceof GeoPointType ||
                column.getNode().getType() instanceof GeoAreaType) {
                continue;
            }
            queryModel.selectField(column.getNode().getFieldId()).as(column.getNode().getFieldId().asString());
        }

        ColumnSet columnSet = assertResolves(locator.queryTable(queryModel));

        XlsColumnSetWriter writer = new XlsColumnSetWriter();
        writer.addSheet(formTree, columnSet);

        File outputDir = new File("target/report-test/");
        outputDir.mkdirs();
        System.out.println("Output dir path: " + outputDir.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream("target/report-test/XlsColumnSetWriterTest.xls");
        writer.write(fos);
        fos.close();
    }
}
