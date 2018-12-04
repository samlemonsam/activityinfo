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
package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.TestBatchFormClassProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.component.importDialog.model.source.PastedTable;
import org.activityinfo.ui.client.page.config.design.importer.SchemaImporterV3;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;


public class SchemaCsvWriterV3Test {


    private UserDatabaseDTO database;
    private FormClass surveyForm;
    private FormClass washForm;
    private TestBatchFormClassProvider formClassProvider;
    private String expectedSurveyExport;

    @Before
    public void setup() throws IOException {

        database = new UserDatabaseDTO(1, "Survey DB");
        formClassProvider = new TestBatchFormClassProvider();

        setupSurveyForm();
        setupMonthly();

        expectedSurveyExport = readExport("survey.csv");
    }

    private void setupSurveyForm() {
        surveyForm = new FormClass(ResourceId.valueOf("FORM1"));
        surveyForm.setLabel("Household Survey");
        surveyForm.addElement(new FormField(ResourceId.valueOf("F1"))
                .setCode("NAME")
                .setLabel("What is your name?")
                .setDescription("The head of household's name")
                .setRequired(true)
                .setType(TextType.SIMPLE));

        surveyForm.addElement(new FormField(ResourceId.valueOf("F2"))
                .setCode("AGE")
                .setLabel("What is your age?")
                .setType(new QuantityType("years"))
                .setRequired(true));

        surveyForm.addElement(new FormField(ResourceId.valueOf("F3"))
                .setCode("GENDER")
                .setLabel("Gender of head of household")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(ResourceId.valueOf("GF"), "Female"),
                        new EnumItem(ResourceId.valueOf("GM"), "Male")))
                .setRequired(true));

        surveyForm.addElement(new FormField(ResourceId.valueOf("F4"))
                .setLabel("Are you currently pregnant?")
                .setRelevanceConditionExpression("AGE > 18 && GENDER == 'Female'")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(ResourceId.valueOf("PY"), "Yes"),
                        new EnumItem(ResourceId.valueOf("PN"), "No")))
                .setRequired(true));

        surveyForm.addElement(new FormField(ResourceId.valueOf("F5"))
                .setLabel("Remarks")
                .setType(NarrativeType.INSTANCE)
                .setRequired(false));

        FormClass subFormClass = new FormClass(ResourceId.valueOf("FORM2"));
        subFormClass.setSubFormKind(SubFormKind.REPEATING);

        subFormClass.addElement(new FormField(ResourceId.valueOf("F21"))
                .setLabel("Name")
                .setType(TextType.SIMPLE)
                .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F22"))
                .setLabel("Age")
                .setType(new QuantityType("years"))
                .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F23"))
                .setLabel("Gender")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(ResourceId.valueOf("GF"), "Female"),
                        new EnumItem(ResourceId.valueOf("GM"), "Male")))
                .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("F24"))
                .setLabel("Vaccinations")
                .setType(new EnumType(Cardinality.MULTIPLE,
                        new EnumItem(ResourceId.valueOf("V1"), "Measles"),
                        new EnumItem(ResourceId.valueOf("V2"), "Mumps"),
                        new EnumItem(ResourceId.valueOf("V3"), "Rubella"))));


        surveyForm.addElement(new FormField(ResourceId.valueOf("SF"))
                .setLabel("Household members")
                .setType(new SubFormReferenceType(subFormClass.getId())));

        formClassProvider.add(surveyForm);
        formClassProvider.add(subFormClass);
    }

    private void setupMonthly() {


        washForm = new FormClass(ResourceId.valueOf("FORM3"));
        washForm.setLabel("Emergency WASH");
        washForm.addElement(new FormField(ResourceId.valueOf("WF1"))
                .setLabel("Partner")
                .setRequired(true)
                .setType(new ReferenceType(Cardinality.SINGLE, ResourceId.valueOf("PARTNER_FORM"))));

        washForm.addElement(new FormField(ResourceId.valueOf("Population"))
                .setCode("POP")
                .setLabel("Affected Population Size")
                .setType(new QuantityType("households"))
                .setRequired(true));

        washForm.addElement(new FormField(ResourceId.valueOf("Population"))
                .setCode("POP")
                .setLabel("Affected Population Size")
                .setType(new QuantityType("households"))
                .setRequired(true));



        FormClass subFormClass = new FormClass(ResourceId.valueOf("FORM4"));
        subFormClass.setSubFormKind(SubFormKind.WEEKLY);

        subFormClass.addElement(new FormField(ResourceId.valueOf("WSF1"))
                .setLabel("Water Trucking")
                .setType(new QuantityType("households"))
                .setRequired(true));

        subFormClass.addElement(new FormField(ResourceId.valueOf("WSF2"))
                .setLabel("Cholorination")
                .setType(new QuantityType("households"))
                .setRequired(true));

        washForm.addElement(new FormField(ResourceId.valueOf("SF"))
                .setLabel("Monthly Output")
                .setType(new SubFormReferenceType(subFormClass.getId())));


        formClassProvider.add(washForm);
        formClassProvider.add(subFormClass);
    }

    private String readExport(String resourceName) throws IOException {
        return Resources.toString(
                Resources.getResource(SchemaCsvWriterV3.class, resourceName),
                Charsets.UTF_8);
    }

    private PastedTable readExportAsTable(String resourceName) throws IOException {
        PastedTable table = new PastedTable(readExport(resourceName));
        table.parseAllRows();
        return table;
    }

    @Test
    public void exportSurveyForm() throws IOException {

        SchemaCsvWriterV3 writer = new SchemaCsvWriterV3(formClassProvider);

        writer.writeForms(database, Collections.singletonList(surveyForm.getId()));

        System.out.println(writer.toString());

        String actual = writer.toString();

        Assert.assertThat(actual, equalTo(expectedSurveyExport));
    }

    @Test
    public void exportWashForm() throws IOException {
        SchemaCsvWriterV3 writer = new SchemaCsvWriterV3(formClassProvider);
        writer.writeForms(database, Collections.singletonList(washForm.getId()));

        System.out.println(writer.toString());

    }

    @Test
    public void importSurveyForm() {

        PastedTable pastedTable = new PastedTable(expectedSurveyExport);
        SchemaImporterV3 importer = new SchemaImporterV3(database.getId(), null, null);

        assertTrue("columns found", importer.parseColumns(pastedTable));
        boolean success = importer.processRows();

        for (SafeHtml warning : importer.getWarnings()) {
            System.out.println(warning);
        }

        assertTrue(success);

        List<FormClass> formClasses = importer.toSave();
        formClassProvider.addAll(formClasses);

        FormTreeBuilder treeBuilder = new FormTreeBuilder(formClassProvider);

        FormTreePrettyPrinter prettyPrinter = new FormTreePrettyPrinter();

        for (FormClass formClass : formClasses) {
            if(!formClass.isSubForm()) {
                prettyPrinter.printTree(treeBuilder.queryTree(formClass.getId()));
            }
        }
    }

    @Test
    public void missingFieldNames() throws IOException {

        PastedTable pastedTable = readExportAsTable("malformed.csv");

        SchemaImporterV3 importer = new SchemaImporterV3(database.getId(), null, null);

        assertTrue("columns found", importer.parseColumns(pastedTable));

        boolean success = importer.processRows();

        for (SafeHtml warning : importer.getWarnings()) {
            System.out.println(warning);
        }

        assertTrue("warnings emitted", importer.getWarnings().size() == 2);
        assertTrue("parsing failed", !success);
    }

    @Test
    public void missingReferences() throws IOException {

        PastedTable pastedTable = readExportAsTable("malformed-ref.csv");

        System.out.println(pastedTable);

        SchemaImporterV3 importer = new SchemaImporterV3(database.getId(), null, null);

        assertTrue(importer.parseColumns(pastedTable));

        boolean success = importer.processRows();

        for (SafeHtml warning : importer.getWarnings()) {
            System.out.println(warning);
        }

        assertTrue("warnings emitted", importer.getWarnings().size() == 1);
        assertTrue("parsing failed", !success);
    }

}