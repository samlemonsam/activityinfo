package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Connection;
import org.activityinfo.store.testing.IdpLocationForm;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.store.testing.TestingCatalog;
import org.activityinfo.store.testing.VillageForm;
import org.activityinfo.ui.client.input.viewModel.PermissionFilters;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class LookupViewModelTest {

    private TestSetup setup = new TestSetup();
    private TestingCatalog catalog;
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
        assertThat(province.getLevelLabel(), equalTo("Province Name"));

        // The second level should now reflect these choices
        LookupKeyViewModel territory = viewModel.getLookupKeys().get(1);
        assertThat(territory.getLevelLabel(), equalTo("Territory Name"));

        // Finally the third level is where we choose the village
        LookupKeyViewModel village = viewModel.getLookupKeys().get(2);
        assertThat(village.getLevelLabel(), equalTo("Name"));

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

        TestingCatalog catalog = new TestingCatalog();
        NfiForm nfiForm = catalog.getNfiForm();

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());

        LookupViewModel viewModel = new LookupViewModel(setup.getFormStore(), formTree,
                new ReferenceType(Cardinality.SINGLE, nfiForm.getFormId()));

        LookupKeyViewModel keyViewModel = viewModel.getLookupKeys().get(0);

        Connection<List<String>> choices = setup.connect(keyViewModel.getChoices());

        assertThat(choices.assertLoaded(), hasSize(821));

    }

    @Test
    public void partnerTest() {


    }

}