package org.activityinfo.ui.client.importer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.core.server.type.converter.JvmConverterFactory;
import org.activityinfo.core.shared.form.tree.FormTreePrettyPrinter;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.strategy.FieldImportStrategies;
import org.activityinfo.core.shared.importing.validation.ValidatedRowTable;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.component.importDialog.Importer;
import org.activityinfo.ui.client.component.importDialog.data.PastedTable;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/somalia-admin.db.xml")
public class ImportGeoTest extends AbstractImporterTest {

    @Before
    public void setupLocale() {
        LocaleProxy.initialize();
    }

    @Test
    public void test() throws IOException {

        FormTree formTree = assertResolves(formTreeBuilder.apply(CuidAdapter.locationFormClass(1451)));
        FormTreePrettyPrinter.print(formTree);

        importModel = new ImportModel(formTree);


        // Step 1: User pastes in data to import
        PastedTable source = new PastedTable(
                Resources.toString(getResource("org/activityinfo/core/shared/importing/somali-camps-cleaned.txt"),
                        Charsets.UTF_8));
        source.parseAllRows();

        importModel.setSource(source);
        importer = new Importer(resourceLocator, formTree, FieldImportStrategies.get(JvmConverterFactory.get()));

        assertThat(importer.getImportTargets(), contains(
                hasProperty("label", Matchers.equalTo("Name")),
                hasProperty("label", Matchers.equalTo("Alternate Name")),
                hasProperty("label", Matchers.equalTo("Region Name")),
                hasProperty("label", Matchers.equalTo("District Name")),
                hasProperty("label", Matchers.equalTo("Latitude")),
                hasProperty("label", Matchers.equalTo("Longitude"))));

        dumpList("COLUMNS", source.getColumns());

        // Step 2: User maps imported columns to FormFields
        dumpList("FIELDS", importer.getImportTargets());
        importModel.setColumnAction(columnIndex("Region"), target("Region Name"));
        importModel.setColumnAction(columnIndex("Admin2"), target("District Name"));
        importModel.setColumnAction(columnIndex("Village Name"), target("Name"));
        importModel.setColumnAction(columnIndex("Pcode"), target("Alternate Name"));
        importModel.setColumnAction(columnIndex("Latitude"), target("Latitude"));
        importModel.setColumnAction(columnIndex("Longitude"), target("Longitude"));


        // Step 3: Validate for user
        ValidatedRowTable validatedResult = assertResolves(importer.validateRows(importModel));
        showValidationGrid(validatedResult);

        assertResolves(importer.persist(importModel));
    }
}
