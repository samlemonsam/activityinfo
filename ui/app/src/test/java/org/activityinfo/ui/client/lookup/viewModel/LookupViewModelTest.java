package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormMetadataProviderStub;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.StringArrayColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.TestSetup;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LookupViewModelTest {

    private TestSetup setup = new TestSetup();
    private TestingStorageProvider catalog;
    private NfiForm nfiForm;
    private VillageForm villageForm;

    @BeforeClass
    public static void setupLocale() {
        LocaleProxy.initialize();
    }


    @Before
    public void setup() {
        catalog = setup.getCatalog();
        nfiForm = catalog.getNfiForm();
        villageForm = catalog.getVillageForm();
    }

    @Test
    public void select() {

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());
        ReferenceType referenceType = (ReferenceType) nfiForm.getVillageField().getType();

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree, referenceType);

        LookupKeyViewModel province = viewModel.getLookupKeys().get(0);
        assertThat(province.getKeyLabel(), equalTo("Province Name"));

        // The second level should now reflect these choices
        LookupKeyViewModel territory = viewModel.getLookupKeys().get(1);
        assertThat(territory.getKeyLabel(), equalTo("Territory Name"));

        // Finally the third level is where we choose the village
        LookupKeyViewModel village = viewModel.getLookupKeys().get(2);
        assertThat(village.getKeyLabel(), equalTo("Village Name"));

        // Connect to the lists
        Connection<List<String>> provinceList = setup.connect(province.getChoices());
        Connection<List<String>> territoryList = setup.connect(territory.getChoices());
        Connection<List<String>> villageList = setup.connect(village.getChoices());

        // Connect to the selection
        Connection<Optional<RecordRef>> selection = setup.connect(viewModel.getSelectedRecord());

        // Initially... no selection
        assertThat(selection.assertLoaded().isPresent(), equalTo(false));

        // Initially the root level should have all choices (16)
        assertThat(provinceList.assertLoaded(), hasSize(16));

        // Select an item in the first level, should change
        // the options available to the second level
        viewModel.select(province.getLookupKey(), province.getChoices().get().get(2));
        territoryList.assertChanged();

        System.out.println(territoryList.assertLoaded());

        assertThat(territoryList.assertLoaded(), hasSize(7));

        System.out.println(territoryList.assertLoaded());

        // Select a territory
        viewModel.select(territory.getLookupKey(), territoryList.assertLoaded().get(2));
        villageList.assertChanged();

        // Select a village
        viewModel.select(village.getLookupKey(), villageList.assertLoaded().get(5));

        assertThat(selection.assertLoaded().get(), equalTo(new RecordRef(villageForm.getFormId(), ResourceId.valueOf("c207"))));
    }

    @Test
    public void initialSetup() {

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());
        ReferenceType referenceType = (ReferenceType) nfiForm.getVillageField().getType();

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree, referenceType);
        viewModel.select(new RecordRef(villageForm.getFormId(), ResourceId.valueOf("c207")));

        Connection<Optional<String>> provinceLabel = setup.connect(viewModel.getLookupKeys().get(0).getSelectedKey());
        Connection<Optional<String>> territoryLabel = setup.connect(viewModel.getLookupKeys().get(1).getSelectedKey());
        Connection<Optional<String>> villageLabel = setup.connect(viewModel.getLookupKeys().get(2).getSelectedKey());

        assertThat(provinceLabel.assertLoaded().get(), equalTo("Province 11"));
        assertThat(territoryLabel.assertLoaded().get(), equalTo("Territory 24"));
        assertThat(villageLabel.assertLoaded().get(), equalTo("Village 208"));

        // Change the territory

        Connection<List<String>> territoryChoices = setup.connect(viewModel.getLookupKeys().get(1).getChoices());
        String newTerritory = territoryChoices.assertLoaded().get(3);
        viewModel.select(viewModel.getLookupKeys().get(1).getLookupKey(), newTerritory);

        // Check updated labels
        assertThat(provinceLabel.assertLoaded().get(), equalTo("Province 11"));
        assertThat(territoryLabel.assertLoaded().get(), equalTo(newTerritory));
        assertThat(villageLabel.assertLoaded().isPresent(), equalTo(false));
    }

    @Test
    public void multipleLeafKeys() {

        IdpLocationForm locationForm = catalog.getIdpLocationForm();
        FormTree formTree = catalog.getFormTree(locationForm.getFormId());

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree,
                new ReferenceType(Cardinality.SINGLE, locationForm.getFormId()));

        assertThat(viewModel.getLookupKeys(), hasSize(3));

        LookupKeyViewModel province = viewModel.getLookupKeys().get(0);
        LookupKeyViewModel type = viewModel.getLookupKeys().get(1);
        LookupKeyViewModel name = viewModel.getLookupKeys().get(2);

        Connection<List<String>> provinceChoices = setup.connect(province.getChoices());
        Connection<List<String>> typeChoices = setup.connect(type.getChoices());
        Connection<List<String>> nameChoices = setup.connect(name.getChoices());

        Connection<Optional<RecordRef>> selection = setup.connect(viewModel.getSelectedRecord());

        viewModel.select(province.getLookupKey(), provinceChoices.assertLoaded().get(0));

        assertThat(typeChoices.assertLoaded(), hasItems(equalTo("Camp"), equalTo("Clinic"), equalTo("School")));

        viewModel.select(type.getLookupKey(), "Camp");

        assertThat(nameChoices.assertLoaded(), hasItems("Location 34", "Location 54", "Location 76"));

        viewModel.select(name.getLookupKey(), "Location 76");

        assertThat(selection.assertLoaded().get(), equalTo(new RecordRef(locationForm.getFormId(), ResourceId.valueOf("c75"))));
    }

    @Test
    public void noKeysTest() {

        TestingStorageProvider catalog = new TestingStorageProvider();
        NfiForm nfiForm = catalog.getNfiForm();

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree,
                new ReferenceType(Cardinality.SINGLE, nfiForm.getFormId()));

        LookupKeyViewModel keyViewModel = viewModel.getLookupKeys().get(0);

        Connection<List<String>> choices = setup.connect(keyViewModel.getChoices());

        assertThat(choices.assertLoaded(), hasSize(821));

    }

    @Test
    public void nullKeys() {

        FormClass keyForm = new FormClass(ResourceId.valueOf("FORM1"));
        keyForm.addField(ResourceId.valueOf("PROVINCE"))
                .setLabel("Province")
                .setKey(true)
                .setRequired(true)
                .setType(TextType.SIMPLE);
        keyForm.addField(ResourceId.valueOf("SCHOOL"))
                .setLabel("School")
                .setKey(true)
                .setRequired(true)
                .setType(TextType.SIMPLE);

        FormClass form = new FormClass(ResourceId.valueOf("FORM2"));
        ReferenceType referenceType = new ReferenceType(Cardinality.SINGLE, ResourceId.valueOf("FORM1"));
        form.addField(ResourceId.valueOf("PROJECT"))
                .setLabel("Project name")
                .setType(referenceType);

        FormTreeBuilder treeBuilder = new FormTreeBuilder(new FormMetadataProviderStub(form, keyForm));
        FormTree formTree = treeBuilder.queryTree(form.getId());

        Map<String, ColumnView> columnSet = new HashMap<>();
        columnSet.put("id", new StringArrayColumnView(Arrays.asList("R1", "R2", "R3", "R4")));
        columnSet.put("k1", new StringArrayColumnView(Arrays.asList("PZ", null, "PA", "PA")));
        columnSet.put("k2", new StringArrayColumnView(Arrays.asList("S1", "S2", null, "S3")));

        FormSource formSource = EasyMock.createMock(FormSource.class);
        EasyMock.expect(formSource.query(EasyMock.anyObject(QueryModel.class)))
                .andReturn(Observable.just(new ColumnSet(3, columnSet)))
                .anyTimes();
        EasyMock.replay(formSource);

        LookupViewModel viewModel = new LookupViewModel(formSource, formTree, referenceType);

        LookupKeyViewModel provinceKey = viewModel.getLookupKeys().get(0);

        assertThat(provinceKey.getChoices().get(), contains("PA", "PZ"));

        viewModel.select(provinceKey.getLookupKey(), "PA");

        LookupKeyViewModel schoolKey = viewModel.getLookupKeys().get(1);
        Connection<List<String>> schoolChoices = new Connection<>(schoolKey.getChoices());

        assertThat(schoolChoices.assertLoaded(), contains("S3"));
    }

    @Test
    public void overlappingHierarchies() {

        LocaliteForm locationForm = catalog.getLocaliteForm();
        FormTree formTree = catalog.getFormTree(locationForm.getFormId());

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree, locationForm.getAdminFieldType());

        assertThat(viewModel.getLookupKeys(), hasSize(3));

        LookupKeyViewModel province = viewModel.getLookupKeys().get(0);
        assertThat(province.getKeyLabel(), equalTo("Province Name"));

        // The second level should now reflect these choices
        LookupKeyViewModel territory = viewModel.getLookupKeys().get(1);
        assertThat(territory.getKeyLabel(), equalTo("Territory Name"));

        // We also have a third level -- Health Zone or "Zone de Sante" which
        // is related to province, but NOT territory.
        LookupKeyViewModel healthZone = viewModel.getLookupKeys().get(2);
        assertThat(healthZone.getKeyLabel(), equalTo("Zone de Sante Name"));

        // Connect to the lists
        Connection<List<String>> provinceList = setup.connect(province.getChoices());
        Connection<List<String>> territoryList = setup.connect(territory.getChoices());
        Connection<List<String>> zoneList = setup.connect(healthZone.getChoices());

        // Connect to the selection
        Connection<Set<RecordRef>> selection = setup.connect(viewModel.getSelectedRecords());

        // Initially... no selection
        assertThat(selection.assertLoaded().isEmpty(), equalTo(true));

        // Initially the root level should have all choices (16)
        assertThat(provinceList.assertLoaded(), hasSize(catalog.getProvince().getCount()));

        // Select a province.
        viewModel.select(province.getLookupKey(), province.getChoices().get().get(2));

        // Choosing just a province is a valid selection, so we should now have a value
        assertThat(selection.assertLoaded(), hasSize(1));

        // ..and the options available to the second level
        territoryList.assertChanged();

        System.out.println(territoryList.assertLoaded());

        assertThat(territoryList.assertLoaded(), hasSize(7));

        System.out.println(territoryList.assertLoaded());

        // Select a territory
        viewModel.select(territory.getLookupKey(), territoryList.assertLoaded().get(2));
        zoneList.assertChanged();

        // No we should still have only one selected record, but this time
        // territory and not province because province is a parent of territory.
        RecordRef territoryRef = new RecordRef(catalog.getTerritory().getFormId(), ResourceId.valueOf("c23"));
        assertThat(selection.assertLoaded(), contains(territoryRef));

        // We should ALSO be able to select a Zone de Sante
        viewModel.select(healthZone.getLookupKey(), zoneList.assertLoaded().get(5));

        RecordRef zoneRef = new RecordRef(catalog.getHealthZone().getFormId(), ResourceId.valueOf("c165"));

        assertThat(selection.assertLoaded(), containsInAnyOrder(territoryRef, zoneRef));


    }

    @Test
    public void overlappingHierarchiesInitialSelection() {

        LocaliteForm locationForm = catalog.getLocaliteForm();
        FormTree formTree = catalog.getFormTree(locationForm.getFormId());

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree, locationForm.getAdminFieldType());

        RecordRef territoryRef = new RecordRef(catalog.getTerritory().getFormId(), ResourceId.valueOf("c23"));

        Connection<Optional<String>> province = setup.connect(viewModel.getLookupKeys().get(0).getSelectedKey());
        Connection<Optional<String>> territory = setup.connect(viewModel.getLookupKeys().get(1).getSelectedKey());
        Connection<Optional<String>> healthZone = setup.connect(viewModel.getLookupKeys().get(2).getSelectedKey());

        // Initially the key value labels should be empty
        assertThat(province.assertLoaded().isPresent(), equalTo(false));
        assertThat(territory.assertLoaded().isPresent(), equalTo(false));
        assertThat(healthZone.assertLoaded().isPresent(), equalTo(false));


        // If the initial selection is a territory then the province and territory labels should be loaded
        viewModel.setInitialSelection(Collections.singleton(territoryRef));
        assertThat(province.assertLoaded().get(), equalTo("Province 11"));
        assertThat(territory.assertLoaded().get(), equalTo("Territory 24"));
        assertThat(healthZone.assertLoaded().isPresent(), equalTo(false));


    }
}