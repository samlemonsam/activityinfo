package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.observable.Connection;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.store.testing.TestingCatalog;
import org.activityinfo.store.testing.VillageForm;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoice;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
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
        Connection<List<ReferenceChoice>> provinceList = setup.connect(province.getChoices());
        Connection<List<ReferenceChoice>> territoryList = setup.connect(territory.getChoices());
        Connection<List<ReferenceChoice>> villageList = setup.connect(village.getChoices());


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

        assertThat(viewModel.getSelectedRecord().get(), equalTo(new RecordRef(villageForm.getFormId(), ResourceId.valueOf("c207"))));
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

        Connection<List<ReferenceChoice>> territoryChoices = setup.connect(viewModel.getLookupKeys().get(1).getChoices());
        ReferenceChoice newTerritory = territoryChoices.assertLoaded().get(3);
        viewModel.select(viewModel.getLookupKeys().get(1).getLookupKey(), newTerritory);

        // Check updated labels
        assertThat(provinceLabel.assertLoaded().get(), equalTo("Province 11"));
        assertThat(territoryLabel.assertLoaded().get(), equalTo(newTerritory.getLabel()));
        assertThat(villageLabel.assertLoaded().isPresent(), equalTo(false));

    }
}