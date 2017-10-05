package org.activityinfo.ui.client.input.viewModel;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.store.testing.AdminLevelForm;
import org.activityinfo.store.testing.NfiForm;
import org.activityinfo.store.testing.TestingCatalog;
import org.activityinfo.store.testing.VillageForm;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class LookupKeySetTest {

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

        assertThat(lookupKeySet.getLeafKeys().get(0).getKeys().values(), contains(
            villageName,
            territoryName,
            provinceName));
    }
}