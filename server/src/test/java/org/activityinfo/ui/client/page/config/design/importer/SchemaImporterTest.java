package org.activityinfo.ui.client.page.config.design.importer;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.endpoint.rest.SchemaCsvWriter;
import org.activityinfo.ui.client.component.importDialog.data.PastedTable;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class SchemaImporterTest extends CommandTestCase2 {


    @Test
    public void syria() throws IOException {

        UserDatabaseDTO syria = doImport("schema_1064.csv");

        int activityId = syria.getActivities().get(0).getId();
        ActivityFormDTO cash = execute(new GetActivityForm(activityId));

        for(AttributeGroupDTO group : cash.getAttributeGroups()) {
            System.out.println(group.getName());
        }

        assertThat(cash.getName(), equalTo("1.Provision of urgent cash assistance"));
        assertThat(cash.getAttributeGroups().size(), equalTo(3));


        SchemaCsvWriter writer = new SchemaCsvWriter(getDispatcherSync());
        writer.write(syria.getId());

        Files.write(writer.toString(), new File("target/syria.csv"), Charsets.UTF_8);
    }

    @Test
    public void southSudan() throws IOException {
        UserDatabaseDTO db = doImport("schema_1321.csv");
        int activityId = db.getActivities().get(0).getId();
        ActivityFormDTO h2 = execute(new GetActivityForm(activityId));
        assertThat(h2.getName(), equalTo("H2"));
        assertThat(h2.getCategory(), equalTo("Health"));
    }

    @Test // AI-678 : Database import duplicates fields
    public void duplicatesTest() throws IOException {
        SchemaImporter schemaImporter = new SchemaImporter(getDispatcher(), db(), warningTemplates());
        schemaImporter.parseColumns(source("schema_ai_678.txt"));
        schemaImporter.processRows();

        assertNoDuplicates(schemaImporter.getNewIndicators());
        assertNoDuplicates(schemaImporter.getNewAttributeGroups());
        assertNoDuplicates(schemaImporter.getNewAttributes());
    }

    private static void assertNoDuplicates(Collection<?> list) {
        if (hasDuplicates(list)) {
            throw new AssertionError("List has duplications:" + list);
        }
    }

    private static boolean hasDuplicates(Collection<?> list) {
        Set set = new HashSet(list);
        return set.size() < list.size();
    }

    private static PastedTable source(String resourceName) throws IOException {
        String csv = Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        return new PastedTable(csv);
    }

    private UserDatabaseDTO db() {
        SchemaDTO schema = execute(new GetSchema());
        return schema.getDatabaseById(1);
    }

    private SchemaImporter.WarningTemplates warningTemplates() {
        SchemaImporter.WarningTemplates templates = EasyMock.createNiceMock(SchemaImporter.WarningTemplates.class);
        EasyMock.replay(templates);
        return templates;
    }

    private UserDatabaseDTO doImport(String resourceName) throws IOException {
        Map<String, Object> dbProps = Maps.newHashMap();
        dbProps.put("name", "Syria");
        dbProps.put("countryId", 1);

        int databaseId = execute(new CreateEntity("UserDatabase", dbProps)).getNewId();

        SchemaDTO schema = execute(new GetSchema());
        UserDatabaseDTO db = schema.getDatabaseById(databaseId);

        if (db == null) {
            throw new AssertionError("database not created");
        }

        SchemaImporter importer = new SchemaImporter(getDispatcher(), db, warningTemplates());
        importer.setProgressListener(new SchemaImporter.ProgressListener() {

            @Override
            public void submittingBatch(int batchNumber, int batchCount) {
                System.out.println("Submitting batch " + batchNumber + " of " + batchCount);
            }
        });

        boolean success = importer.parseColumns(source(resourceName));
        if (success) {
            importer.processRows();
        }

        for (SafeHtml warning : importer.getWarnings()) {
            System.err.println(warning);
        }

        if (!success) {
            throw new AssertionError("there were fatal errors");
        }

        importer.persist(new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                System.out.println("Success");
            }

            @Override
            public void onFailure(Throwable caught) {
                throw new AssertionError(caught);
            }
        });

        return execute(new GetSchema()).getDatabaseById(databaseId);
    }

}
