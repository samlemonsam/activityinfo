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
package org.activityinfo.ui.client.formulaDialog;

import org.activityinfo.analysis.FormulaValidator;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.TestBatchFormClassProvider;
import org.activityinfo.model.formula.FormulaError;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.SourcePos;
import org.activityinfo.model.formula.SourceRange;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.store.testing.TestingStorageProvider;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FormulaValidatorTest {

    private TestingStorageProvider catalog = new TestingStorageProvider();

    @Test
    public void badSymbols() {

        FormulaValidator validator = validate("XX+ZZ");

        assertThat(validator.getErrors(), hasSize(2));
    }


    @Test
    public void goodSymbols() {
        FormulaValidator validator = validate("AGE");

        assertThat(validator.getErrors(), hasSize(0));
        assertTrue(validator.isValid());
        assertThat(validator.getResultType(), instanceOf(QuantityType.class));
    }

    @Test
    public void validTypeArguments() {
        FormulaValidator validator = validate("IF(TRUE, 1, 0)");
        assertThat(validator.getErrors(), hasSize(0));
        assertTrue(validator.isValid());
        assertThat(validator.getResultType(), instanceOf(QuantityType.class));
    }

    @Test
    public void badNumberOfArguments() {
        FormulaValidator validator = validate("IF(1)");
        assertThat(validator.getErrors(), hasSize(1));
        assertThat(validator.getErrors().get(0).getSourceRange(), equalTo(new SourceRange(new SourcePos(0, 0), 5)));
    }

    @Test
    public void invalidTypeArguments() {
        FormulaValidator validator = validate("IF('Foo', 1, 0)");
        assertThat(validator.getErrors(), hasSize(1));
        assertFalse(validator.isValid());
    }

    @Test
    public void booleanEnumReference() {
        FormulaValidator validator = validate("Gender.Female || Gender.Male");
        assertThat(validator.getErrors(), hasSize(0));
        assertTrue(validator.isValid());
        assertThat(validator.getResultType(), equalTo(BooleanType.INSTANCE));
    }

    private FormulaValidator validate(String formula) {
        Survey surveyForm = catalog.getSurvey();
        FormulaValidator validator = new FormulaValidator(catalog.getFormTree(surveyForm.getFormId()));
        validator.validate(FormulaParser.parse(formula));

        for (FormulaError error : validator.getErrors()) {
            System.out.println("Error at " + error.getSourceRange() + ": " + error.getMessage());
        }

        return validator;
    }


    @Test
    public void invalidFormSchema() {
        FormClass formClass = new FormClass(ResourceId.valueOf("XYZ"));
        formClass.addField(ResourceId.valueOf("F1"))
                .setCode("A")
                .setLabel("Field A1")
                .setType(TextType.SIMPLE);
        formClass.addField(ResourceId.valueOf("F2"))
                .setCode("A")
                .setLabel("Field A2")
                .setType(TextType.SIMPLE);

        TestBatchFormClassProvider formProvider = new TestBatchFormClassProvider();
        formProvider.add(formClass);

        FormTreeBuilder formTreeBuilder = new FormTreeBuilder(formProvider);
        FormTree formTree = formTreeBuilder.queryTree(formClass.getId());

        FormulaValidator validator = new FormulaValidator(formTree);
        assertFalse(validator.validate(FormulaParser.parse("A")));
        assertThat(validator.getErrors(), hasSize(1));
    }



}