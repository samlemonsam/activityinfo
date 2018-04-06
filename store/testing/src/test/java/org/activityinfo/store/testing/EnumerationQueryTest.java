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
package org.activityinfo.store.testing;

import org.activityinfo.model.formula.CompoundExpr;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.EnumColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.query.shared.columns.MultiDiscreteStringColumnView;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnumerationQueryTest {

    private static final String NAT_PAL = "Palestinian";

    @Test
    public void enumRefTests() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        IntakeForm intakeForm = catalog.getIntakeForm();

        QueryModel queryModel = new QueryModel(intakeForm.getFormId());
        queryModel.selectField(intakeForm.getNationalityFieldId()).as("nationality");
        queryModel.selectExpr(intakeForm.getNationalityFieldId() + "==" + "\"" + intakeForm.getPalestinianId() + "\"").as("palestinian");
        queryModel.selectExpr("\"" + intakeForm.getPalestinianId() + "\"" + "==" + intakeForm.getNationalityFieldId()).as("palestinianInverse");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView nationality = columnSet.getColumnView("nationality");
        ColumnView palestinian = columnSet.getColumnView("palestinian");
        ColumnView palestinianInverse = columnSet.getColumnView("palestinianInverse");

        assertThat(nationality.get(1).toString(), equalTo(NAT_PAL));
        assertThat(Boolean.valueOf(palestinian.get(1).toString()), equalTo(true));
        assertThat(Boolean.valueOf(palestinianInverse.get(1).toString()), equalTo(true));
    }

    @Test
    public void enumIdTests() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(EmptyForm.FORM_ID);
        queryModel.selectField(EmptyForm.ENUM_FIELD_ID).as("emptyEnum");
        queryModel.selectField(EmptyForm.POP_ENUM_FIELD_ID).as("populatedEnum");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView emptyEnum = columnSet.getColumnView("emptyEnum");
        ColumnView populatedEnum = columnSet.getColumnView("populatedEnum");

        //// Standard Enum Id Checks
        // Null check on empty set first
        assertThat(emptyEnum.get(0), equalTo(null));
        assertThat(((EnumColumnView) emptyEnum).getId(0), equalTo(null));
        // Value check on populated set
        assertThat(populatedEnum.get(0).toString(), equalTo("One"));
        assertThat(((EnumColumnView) populatedEnum).getId(0), equalTo(EmptyForm.ENUM_ONE_ID.asString()));

        //// Selected Row Enum Id Checks
        ColumnView emptySelectedRow = emptyEnum.select(new int[]{0});
        ColumnView populatedSelectedRow = populatedEnum.select(new int[]{0});

        // Null check on empty set first
        assertThat(emptySelectedRow.get(0), equalTo(null));
        assertThat(((EnumColumnView) emptySelectedRow).getId(0), equalTo(null));
        // Value check on populated set
        assertThat(populatedSelectedRow.get(0).toString(), equalTo("One"));
        assertThat(((EnumColumnView) populatedSelectedRow).getId(0), equalTo(EmptyForm.ENUM_ONE_ID.asString()));
    }

    @Test
    public void multiEnumColumnLabels() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        Survey surveyForm = catalog.getSurvey();

        QueryModel queryModel = new QueryModel(surveyForm.getFormId());
        queryModel.selectField(surveyForm.getGenderFieldId()).as("gender");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthTv().asString())).as("wealth.tv");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthRadio().asString())).as("wealth.radio");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthFridge().asString())).as("wealth.fridge");
        queryModel.selectField(surveyForm.getWealthFieldId()).as("wealth");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView wealthColumn = columnSet.getColumnView("wealth");
        ColumnView tvColumn = columnSet.getColumnView("wealth.tv");
        ColumnView radioColumn = columnSet.getColumnView("wealth.radio");
        ColumnView fridgeColumn = columnSet.getColumnView("wealth.fridge");

        // Radio
        assertThat(wealthColumn.getString(0),   equalTo(surveyForm.getWealthRadioLabel()));
        assertThat(tvColumn.getBoolean(0),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(0),   equalTo(ColumnView.TRUE));
        assertThat(fridgeColumn.getBoolean(0),  equalTo(ColumnView.FALSE));

        // Null
        assertThat(wealthColumn.getString(1),   nullValue());
        assertThat(tvColumn.getBoolean(1),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(1),   equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(1),  equalTo(ColumnView.FALSE));

        // TV
        assertThat(wealthColumn.getString(7),   equalTo(surveyForm.getWealthTvLabel()));
        assertThat(tvColumn.getBoolean(7),      equalTo(ColumnView.TRUE));
        assertThat(radioColumn.getBoolean(7),   equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(7),  equalTo(ColumnView.FALSE));

        // Fridge
        assertThat(wealthColumn.getString(11),   equalTo(surveyForm.getWealthFridgeLabel()));
        assertThat(tvColumn.getBoolean(11),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(11),   equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(11),  equalTo(ColumnView.TRUE));

        // Radio AND Fridge
        assertThat(wealthColumn.getString(100),   equalTo(new String(surveyForm.getWealthRadioLabel()
                                                                                + MultiDiscreteStringColumnView.SEPARATOR
                                                                                + surveyForm.getWealthFridgeLabel())));
        assertThat(tvColumn.getBoolean(100),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(100),   equalTo(ColumnView.TRUE));
        assertThat(fridgeColumn.getBoolean(100),  equalTo(ColumnView.TRUE));
    }

    @Test
    public void multiEnumColumnIds() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        Survey surveyForm = catalog.getSurvey();

        QueryModel queryModel = new QueryModel(surveyForm.getFormId());
        queryModel.selectField(surveyForm.getGenderFieldId()).as("gender");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthTv().asString())).as("wealth.tv");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthRadio().asString())).as("wealth.radio");
        queryModel.selectExpr(new CompoundExpr(surveyForm.getWealthFieldId(), surveyForm.getWealthFridge().asString())).as("wealth.fridge");
        queryModel.selectField(surveyForm.getWealthFieldId()).as("wealth");

        ColumnSet columnSet = builder.build(queryModel);
        EnumColumnView wealthColumn = (EnumColumnView) columnSet.getColumnView("wealth");
        ColumnView tvColumn = columnSet.getColumnView("wealth.tv");
        ColumnView radioColumn = columnSet.getColumnView("wealth.radio");
        ColumnView fridgeColumn = columnSet.getColumnView("wealth.fridge");

        // Radio
        assertThat(wealthColumn.getId(0),       equalTo(surveyForm.getWealthRadio().asString()));
        assertThat(tvColumn.getBoolean(0),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(0),   equalTo(ColumnView.TRUE));
        assertThat(fridgeColumn.getBoolean(0),  equalTo(ColumnView.FALSE));

        // Null
        assertThat(wealthColumn.getId(1),       nullValue());
        assertThat(tvColumn.getBoolean(1),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(1),   equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(1),  equalTo(ColumnView.FALSE));

        // TV
        assertThat(wealthColumn.getId(7),       equalTo(surveyForm.getWealthTv().asString()));
        assertThat(tvColumn.getBoolean(7),      equalTo(ColumnView.TRUE));
        assertThat(radioColumn.getBoolean(7),   equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(7),  equalTo(ColumnView.FALSE));

        // Fridge
        assertThat(wealthColumn.getId(11),      equalTo(surveyForm.getWealthFridge().asString()));
        assertThat(tvColumn.getBoolean(11),     equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(11),  equalTo(ColumnView.FALSE));
        assertThat(fridgeColumn.getBoolean(11), equalTo(ColumnView.TRUE));

        // Radio AND Fridge
        assertThat(wealthColumn.getId(100),     equalTo(new String(surveyForm.getWealthRadio().asString()
                                                                        + MultiDiscreteStringColumnView.SEPARATOR
                                                                        + surveyForm.getWealthFridge().asString())));
        assertThat(tvColumn.getBoolean(100),      equalTo(ColumnView.FALSE));
        assertThat(radioColumn.getBoolean(100),   equalTo(ColumnView.TRUE));
        assertThat(fridgeColumn.getBoolean(100),  equalTo(ColumnView.TRUE));
    }

}