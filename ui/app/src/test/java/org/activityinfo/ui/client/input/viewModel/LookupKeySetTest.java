package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.*;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class LookupKeySetTest {

    private TestSetup setup = new TestSetup();

    @BeforeClass
    public static void setupLocale() {
        LocaleProxy.initialize();
    }

    @Test
    public void hierarchyTest() {
        TestingCatalog catalog = new TestingCatalog();
        NfiForm nfiForm = catalog.getNfiForm();
        VillageForm villageForm = catalog.getVillageForm();
        AdminLevelForm territoryForm = catalog.getVillageForm().getParentForm();
        AdminLevelForm provinceForm = territoryForm.getParentForm().get();

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());
        ReferenceType referenceType = (ReferenceType) nfiForm.getVillageField().getType();

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, referenceType);

        assertThat(lookupKeySet.getLookupKeys().get(0).getLevelLabel(), equalTo("Province Name"));
        assertThat(lookupKeySet.getLookupKeys().get(1).getLevelLabel(), equalTo("Territory Name"));
        assertThat(lookupKeySet.getLookupKeys().get(2).getLevelLabel(), equalTo("Name"));

        SymbolExpr villageName = Exprs.symbol(villageForm.getNameField().getId());
        SymbolExpr villageTerritory = new SymbolExpr(villageForm.getAdminFieldId());
        ExprNode territoryName = new CompoundExpr(villageTerritory, territoryForm.getNameFieldId());
        ExprNode territoryProvince = new CompoundExpr(villageTerritory, territoryForm.getParentFieldId());
        ExprNode provinceName = new CompoundExpr(territoryProvince, provinceForm.getNameFieldId());

        assertThat(lookupKeySet.getLeafKeys().get(0).getKeyFormulas().values(), containsInAnyOrder(
            villageName,
            territoryName,
            provinceName));
    }

    @Test
    public void labelTest() {
        NfiForm nfiForm = setup.getNfiForm();
        VillageForm villageForm = setup.getVillageForm();
        AdminLevelForm territoryForm = villageForm.getParentForm();
        AdminLevelForm provinceForm = territoryForm.getParentForm().get();

        Observable<Maybe<RecordTree>> nfiRecordTree = setup.getFormStore().getRecordTree(nfiForm.getRecordRef(0));

        Connection<Maybe<RecordTree>> nfiRecordTreeView = setup.connect(nfiRecordTree);

        RecordTree tree = nfiRecordTreeView.assertLoaded().get();

        LookupKeySet lookupKeySet = new LookupKeySet(
                tree.getFormTree(),
                tree.getFormTree().getRootField(nfiForm.getVillageField().getId()).getField()
        );

        assertThat(lookupKeySet.getLookupKeys().size(), equalTo(3));

        ReferenceValue referenceValue = (ReferenceValue) tree.getRoot().get(nfiForm.getVillageField().getId());
        RecordRef villageRef = referenceValue.getOnlyReference();

        RecordTree villageTree = tree.subTree(villageRef);
        referenceValue = (ReferenceValue) villageTree.getRoot().get(villageForm.getAdminFieldId());
        RecordRef territoryRef = referenceValue.getOnlyReference();

        RecordTree territoryTree = villageTree.subTree(territoryRef);
        referenceValue = (ReferenceValue) territoryTree.getRoot().get(territoryForm.getParentFieldId());
        RecordRef provinceRef = referenceValue.getOnlyReference();

        Maybe<String> villageLabel = lookupKeySet.label(tree, villageRef);
        Maybe<String> territoryLabel = lookupKeySet.label(tree, territoryRef);
        Maybe<String> provinceLabel = lookupKeySet.label(tree, provinceRef);

        assertThat(villageLabel, equalTo(Maybe.of("Village 660")));
        assertThat(territoryLabel, equalTo(Maybe.of("Territory 85")));
        assertThat(provinceLabel, equalTo(Maybe.of("Province 11")));
    }

    @Test
    public void noKeysTest() {

        TestingCatalog catalog = new TestingCatalog();
        NfiForm nfiForm = catalog.getNfiForm();

        ReferenceType type = new ReferenceType(Cardinality.SINGLE, nfiForm.getFormId());

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());
        LookupKeySet lookupKeySet = new LookupKeySet(formTree, type);

        assertThat(lookupKeySet.getLookupKeys(), hasSize(1));
        assertThat(lookupKeySet.getLeafKeys(), hasSize(1));
    }
}