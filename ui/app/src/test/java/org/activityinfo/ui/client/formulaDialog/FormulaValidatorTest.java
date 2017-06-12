package org.activityinfo.ui.client.formulaDialog;

import org.activityinfo.analysis.FormulaValidator;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.FormulaError;
import org.activityinfo.model.expr.SourcePos;
import org.activityinfo.model.expr.SourceRange;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.store.testing.Survey;
import org.activityinfo.store.testing.TestingCatalog;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class FormulaValidatorTest {

    private TestingCatalog catalog = new TestingCatalog();

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
        FormulaValidator validator = new FormulaValidator(catalog.getFormTree(Survey.FORM_ID));
        validator.validate(ExprParser.parse(formula));

        for (FormulaError error : validator.getErrors()) {
            System.out.println("Error at " + error.getSourceRange() + ": " + error.getMessage());
        }

        return validator;
    }


}