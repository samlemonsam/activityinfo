/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.input.viewModel;

import com.google.common.collect.Iterables;
import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.formTree.LookupKeySet;
import org.activityinfo.model.formTree.RecordTree;
import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Connection;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.Maybe;
import org.activityinfo.store.testing.*;
import org.activityinfo.ui.client.lookup.viewModel.LookupViewModel;
import org.activityinfo.ui.client.store.TestSetup;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class LookupKeySetTest {

    private TestSetup setup = new TestSetup();

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

        SymbolNode villageName = Formulas.symbol(villageForm.getNameField().getId());
        SymbolNode villageTerritory = new SymbolNode(villageForm.getAdminFieldId());
        FormulaNode territoryName = new CompoundExpr(villageTerritory, territoryForm.getNameFieldId());
        FormulaNode territoryProvince = new CompoundExpr(villageTerritory, territoryForm.getParentFieldId());
        FormulaNode provinceName = new CompoundExpr(territoryProvince, provinceForm.getNameFieldId());

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

    /**
     * It may be the case that a referenced form may have multiple *parent* forms. This is true for the instance where a
     * Localite or Village can exist in one of multiple types of administrative unit.
     *
     * In this case, you have a hierarchy:
     * <pre>
     *               Province.Name
     *                 ^    ^    ^
     *                 |    |    |
     *     Territory.Name   |   Health Zone.Name
     *                 ^    |    ^
     *                 |    |    |
     *                Village.Name
     *
     * </pre>
     *
     * Therefore the user can select from a Village that can sit within a Province, a Territory, _or_ a Health Zone.
     */
    @Test
    public void overlappingHierarchiesWithMultipleParents() {

        TestingStorageProvider catalog = setup.getCatalog();
        LocationSelectionForm locationSelectionForm = catalog.getLocationSelectionForm();
        FormTree formTree = setup.getFormTree(locationSelectionForm.getFormId());

        LookupKeySet lookupKeySet = new LookupKeySet(formTree, locationSelectionForm.getLocalitieField());

        // The resulting key set should only include 4 keys, not 6
        // because the three different forms in the range overlap

        assertThat(lookupKeySet.getLookupKeys(), hasSize(4));

        assertThat(lookupKeySet.getKey(0).getKeyLabel(), equalTo("Province Name"));
        assertThat(lookupKeySet.getKey(1).getKeyLabel(), equalTo("Territory Name"));
        assertThat(lookupKeySet.getKey(2).getKeyLabel(), equalTo("Zone de Sante Name"));
        assertThat(lookupKeySet.getKey(3).getKeyLabel(), equalTo("Village Name"));

        // We need the relationships between the forms
        ResourceId provinceId = catalog.getProvince().getFormId();
        ResourceId territoryId = catalog.getTerritory().getFormId();
        ResourceId healthZoneId = catalog.getHealthZone().getFormId();
        ResourceId localiteId = catalog.getLocaliteForm().getFormId();

        assertThat(lookupKeySet.getAncestorForms(provinceId), hasSize(0));
        assertThat(lookupKeySet.getAncestorForms(territoryId), contains(provinceId));
        assertThat(lookupKeySet.getAncestorForms(healthZoneId), contains(provinceId));
        assertThat(lookupKeySet.getAncestorForms(localiteId), containsInAnyOrder(provinceId, territoryId, healthZoneId));

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

        Map<LookupKey, FormulaNode> formulas = lookupKeySet.getKeyFormulas(localiteForm.getAdminField().getId());

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

        Map<LookupKey, FormulaNode> formulas = lookupKeySet.getKeyFormulas(localiteForm.getAdminField().getId());

        QueryModel queryModel = new QueryModel(localiteForm.getFormId());
        queryModel.selectRecordId().as("id");
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

    @Test
    public void multipleTextKeysTest() {
        TestingStorageProvider catalog = setup.getCatalog();
        SimpleReferenceForm refForm = catalog.getSimpleReferenceForm();
        MultipleTextKeysForm multTextKeyForm = catalog.getMultipleTextKeysForm();

        FormField refField = refForm.getRefField();
        CompoundExpr pathToKey1 = new CompoundExpr(refField.getId(), MultipleTextKeysForm.FIRST_TEXT_KEY_ID.asString());
        CompoundExpr pathToKey2 = new CompoundExpr(refField.getId(), MultipleTextKeysForm.SECOND_TEXT_KEY_ID.asString());

        FormTree formTree = catalog.getFormTree(multTextKeyForm.getFormId());
        LookupKeySet lookupKeySet = new LookupKeySet(formTree, refField);

        Map<LookupKey,FormulaNode> formulas = lookupKeySet.getKeyFormulas(refField.getId());

        assertThat(formulas.values(), containsInAnyOrder(pathToKey1, pathToKey2));
    }

    @Test
    public void issue2068() {

        FormClass intakeForm = new FormClass(ResourceId.valueOf("INTAKE"));
        intakeForm.setLabel("Intake");
        intakeForm.addField(ResourceId.valueOf("S"))
                .setType(new SerialNumberType())
                .setLabel("Case number");

        FormClass firstForm = new FormClass(ResourceId.valueOf("PHASE1"));
        firstForm.setLabel("Phase 1");
        firstForm.addField(ResourceId.valueOf("K1"))
                .setType(new ReferenceType(Cardinality.SINGLE, intakeForm.getId()))
                .setLabel("Case number")
                .setKey(true);

        FormClass secondForm = new FormClass(ResourceId.valueOf("PHASE2"));
        secondForm.setLabel("Phase 2");
        secondForm.addField(ResourceId.valueOf("K2"))
                .setType(new ReferenceType(Cardinality.SINGLE, firstForm.getId()))
                .setLabel("Case number")
                .setKey(true);


        FormTree formTree = new FormTree(intakeForm.getId());
        formTree.addFormMetadata(FormMetadata.of(1L, intakeForm, FormPermissions.readonly()));
        formTree.addFormMetadata(FormMetadata.of(1L, firstForm, FormPermissions.readonly()));
        formTree.addFormMetadata(FormMetadata.of(1L, secondForm, FormPermissions.readonly()));

        LookupKeySet keySet = new LookupKeySet(formTree, secondForm.getField(ResourceId.valueOf("K2")));
        assertThat(keySet.getLookupKeys(), hasSize(1));
        assertThat(keySet.getLeafKeys(), hasSize(1));

        LookupKey lookupKey = keySet.getLookupKeys().get(0);
        assertThat(lookupKey.getKeyLabel(), equalTo("Case number"));

        Map<LookupKey, FormulaNode> formulas = keySet.getKeyFormulas(new SymbolNode("Z"));
        assertThat(formulas.get(lookupKey).asExpression(), equalTo("Z.S"));

    }
}