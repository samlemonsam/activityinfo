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
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SubFormDeletionTest {

    private TestingStorageProvider testingCatalog;

    @Before
    public void setup() {
        testingCatalog = new TestingStorageProvider();
    }


    @Test
    public void deleteParent() {

        QueryModel queryModel = new QueryModel(ReferralSubForm.FORM_ID);
        queryModel.selectRecordId().as("id");
        queryModel.selectExpr(new CompoundExpr(IncidentForm.FORM_ID, ColumnModel.RECORD_ID_SYMBOL)).as("parent");
        queryModel.selectExpr(new ConstantNode(1)).as("count");

        ColumnSet columnSet = query(queryModel);
        assertThat(columnSet.getNumRows(), equalTo(ReferralSubForm.ROW_COUNT));
        System.out.println(columnSet.getColumnView("parent"));


        // Now delete a number of parent records
        delete(IncidentForm.FORM_ID, "c528");

        // The query results should reflect the change
        columnSet = query(queryModel);

        assertThat(columnSet.getNumRows(), equalTo(ReferralSubForm.ROW_COUNT - 2));

    }

    @Test
    public void parentQueries() {
        QueryModel queryModel = new QueryModel(ReferralSubForm.FORM_ID);
        queryModel.selectRecordId().as("id");
        queryModel.selectExpr("URGENCY").as("Urgency");

        ColumnSet columnSet = query(queryModel);

    }

    private ColumnSet query(QueryModel queryModel) {
        ColumnSetBuilder builder = new ColumnSetBuilder(testingCatalog, new NullFormScanCache(), new NullFormSupervisor());
        return builder.build(queryModel);
    }


    private void delete(ResourceId formId, String recordId) {

        RecordTransactionBuilder tx = new RecordTransactionBuilder();
        tx.delete(formId, ResourceId.valueOf(recordId));

        testingCatalog.updateRecords(tx.build());
    }

}
