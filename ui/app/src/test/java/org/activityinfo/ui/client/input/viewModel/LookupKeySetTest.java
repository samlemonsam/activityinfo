package org.activityinfo.ui.client.input.viewModel;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LookupKeySetTest {

    private TestSetup setup = new TestSetup();

    @BeforeClass
    public static void setupLocale() {
        LocaleProxy.initialize();
    }

    /**
     * In the case of a simple hierarchy, we are referencing a single form that
     * in turn has key reference fields.
     *
     * For example, if the field references a Village form, this should yield:
     *
     * <pre>
     *     Province.Name
     *        ^
     *        |
     *     District.Name
     *        ^
     *        |
     *     Village.Name
     * </pre>
     *
     * All fields are required because we ultimately need the reference to the village record.
     */
    @Test
    public void simpleHierarchyTest() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        NfiForm nfiForm = catalog.getNfiForm();
        VillageForm villageForm = catalog.getVillageForm();
        AdminLevelForm territoryForm = catalog.getVillageForm().getParentForm();
        AdminLevelForm provinceForm = territoryForm.getParentForm().get();

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, nfiForm.getVillageField());

        // Keys need to be topologically sorted,
        // with parent keys preceding child keys in the list
        assertThat(lookupKeySet.getKey(0).getKeyLabel(), equalTo("Province Name"));
        assertThat(lookupKeySet.getKey(1).getKeyLabel(), equalTo("Territory Name"));
        assertThat(lookupKeySet.getKey(2).getKeyLabel(), equalTo("Village Name"));

        SymbolExpr villageName = Exprs.symbol(villageForm.getNameField().getId());
        SymbolExpr villageTerritory = new SymbolExpr(villageForm.getAdminFieldId());
        ExprNode territoryName = new CompoundExpr(villageTerritory, territoryForm.getNameFieldId());
        ExprNode territoryProvince = new CompoundExpr(villageTerritory, territoryForm.getParentFieldId());
        ExprNode provinceName = new CompoundExpr(territoryProvince, provinceForm.getNameFieldId());

        assertThat(lookupKeySet.getLeafKeys(), hasSize(1));

        LookupKey leafKey = Iterables.getOnlyElement(lookupKeySet.getLeafKeys());

        assertThat(leafKey.getKeyFormulas().values(), containsInAnyOrder(
            villageName,
            territoryName,
            provinceName));
    }

    /**
     * For classical location types especially, a reference field's range might include multiple forms,
     * such as [province, territory, health zone].
     *
     * This means that the user can <i>either</i> select a province, <i>or</i> a territory, <i>or</i>
     * a health zone.
     *
     * In this case, you also have a hierarchy
     * <pre>
     *               Province.Name
     *                 ^         ^
     *                 |         |
     *     Territory.Name     Health Zone.Name
     *        ^
     *        |
     *     Sector.Name
     * </pre>
     *
     * But the record reference depends on which fields are completed. If only province is completed,
     * then the selected record is from province. Otherwise from territory, etc.
     *
     */
    @Test
    public void overlappingHierarchies() {

        TestingStorageProvider catalog = setup.getCatalog();
        LocaliteForm localiteForm = catalog.getLocaliteForm();
        FormTree formTree = setup.getFormTree(localiteForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, localiteForm.getAdminField());

        // The resulting key set should only include 3 keys, not 6
        // because the three different forms in the range overlap

        assertThat(lookupKeySet.getLookupKeys(), hasSize(3));

        assertThat(lookupKeySet.getKey(0).getKeyLabel(), equalTo("Province Name"));
        assertThat(lookupKeySet.getKey(1).getKeyLabel(), equalTo("Territory Name"));
        assertThat(lookupKeySet.getKey(2).getKeyLabel(), equalTo("Zone de Sante Name"));

        // We need the relationships between the forms
        ResourceId provinceId = catalog.getProvince().getFormId();
        ResourceId territoryId = catalog.getTerritory().getFormId();

        assertThat(lookupKeySet.getAncestorForms(provinceId), hasSize(0));
        assertThat(lookupKeySet.getAncestorForms(territoryId), contains(provinceId));

        // Formulas...
        for (LookupKey lookupKey : lookupKeySet.getLeafKeys()) {
            System.out.println(lookupKey.getKeyLabel() + " => " + lookupKey.getKeyFormulas());
        }
    }


    @Test
    public void overlappingHierarchiesParse() {

        TestingStorageProvider catalog = setup.getCatalog();
        LocaliteForm localiteForm = catalog.getLocaliteForm();
        FormTree formTree = setup.getFormTree(localiteForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, localiteForm.getAdminField());

        Map<LookupKey, ExprNode> formulas = lookupKeySet.getKeyFormulas(localiteForm.getAdminField().getId());

        ParsedFormula province = new ParsedFormula(formTree, formulas.get(lookupKeySet.getKey(0)).asExpression());
        assertThat(province.isValid(), equalTo(true));
        assertThat(province.getResultType(), instanceOf(TextType.class));
    }

    @Test
    public void overlappingHierarchiesKeyQuery() {

        TestingStorageProvider catalog = setup.getCatalog();
        LocaliteForm localiteForm = catalog.getLocaliteForm();
        FormTree formTree = setup.getFormTree(localiteForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, localiteForm.getAdminField());

        Map<LookupKey, ExprNode> formulas = lookupKeySet.getKeyFormulas(localiteForm.getAdminField().getId());

        QueryModel queryModel = new QueryModel(localiteForm.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr(formulas.get(lookupKeySet.getKey(0))).as("province");
        queryModel.selectExpr(formulas.get(lookupKeySet.getKey(1))).as("territory");
        queryModel.selectExpr(formulas.get(lookupKeySet.getKey(2))).as("zs");

        Connection<ColumnSet> table = setup.connect(setup.getFormStore().query(queryModel));

        ColumnSet columnSet = table.assertLoaded();

        ColumnView id = columnSet.getColumnView("id");
        ColumnView province = columnSet.getColumnView("province");
        ColumnView territory = columnSet.getColumnView("territory");
        ColumnView zs = columnSet.getColumnView("zs");

        // First row references a Zone de Sante
        assertThat(id.getString(0),         equalTo("c0"));
        assertThat(province.getString(0),   equalTo("Province 14"));
        assertThat(territory.getString(0),  nullValue());
        assertThat(zs.getString(0),         equalTo("Zone de Sante 56"));

        // Second row references only a Province
        assertThat(id.getString(1),         equalTo("c1"));
        assertThat(province.getString(1),   equalTo("Province 8"));
        assertThat(territory.getString(1),  nullValue());
        assertThat(zs.getString(1),         nullValue());

        // Fifth row references a territory
        assertThat(id.getString(4),         equalTo("c4"));
        assertThat(province.getString(4),   equalTo("Province 15"));
        assertThat(territory.getString(4),  equalTo("Territory 42"));
        assertThat(zs.getString(4),         nullValue());


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

        TestingStorageProvider catalog = new TestingStorageProvider();
        NfiForm nfiForm = catalog.getNfiForm();

        FormField field = new FormField(ResourceId.generateFieldId(ReferenceType.TYPE_CLASS));
        field.setType(new ReferenceType(Cardinality.SINGLE, nfiForm.getFormId()));

        FormTree formTree = catalog.getFormTree(nfiForm.getFormId());
        LookupKeySet lookupKeySet = new LookupKeySet(formTree, field);

        assertThat(lookupKeySet.getLookupKeys(), hasSize(1));
        assertThat(lookupKeySet.getLeafKeys(), hasSize(1));
    }
}