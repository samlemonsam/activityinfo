package org.activityinfo.ui.client.component.report.editor.pivotTable;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.server.command.CommandTestCase;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.ui.client.MockEventBus;
import org.activityinfo.ui.client.dispatch.DispatcherStub;
import org.activityinfo.ui.client.page.report.ReportChangeEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/pruning.db.xml")
public class DimensionPrunerTest extends CommandTestCase2 {

    private static final int NFI_FUNDING_GROUP_ID = 102;
    private static final int FAIR_FUNDING_GROUP_ID = 103;

    private static final int NB_MENAGES_INDICATOR_ID = 101;
    private static final int VOUCHER_INDICATOR_ID = 102;

    private MockEventBus eventBus = new MockEventBus();
//
//    @Before
//    public void setupData() {
//
//        ActivityFormDTO dist = new ActivityFormDTO(1, "Distribution");
//        IndicatorDTO nbMenages = new IndicatorDTO();
//        nbMenages.setId(NB_MENAGES_INDICATOR_ID);
//        nbMenages.setName("Nb Menages");
//        dist.getIndicators().add(nbMenages);
//
//        AttributeGroupDTO distFunding = new AttributeGroupDTO(NFI_FUNDING_GROUP_ID);
//        distFunding.setName("Funding Source");
//        dist.getAttributeGroups().add(distFunding);
//
//
//        ActivityFormDTO fairs = new ActivityFormDTO(2, "Faire");
//
//        AttributeGroupDTO fairFunding = new AttributeGroupDTO(FAIR_FUNDING_GROUP_ID);
//        fairFunding.setName("Funding Source");
//        fairs.getAttributeGroups().add(fairFunding);
//
//        IndicatorDTO voucherValue = new IndicatorDTO();
//        voucherValue.setId(VOUCHER_INDICATOR_ID);
//        voucherValue.setName("Voucher Value");
//        fairs.getIndicators().add(voucherValue);
//
//        UserDatabaseDTO nfi = new UserDatabaseDTO(1, "NFI");
//        nfi.addActivity(dist);
//        nfi.addActivity(fairs);
//
//        this.schema = new SchemaDTO();
//        schema.getDatabases().add(nfi);
//
//        dispatcher.setResult(GetSchema.class, schema);
//    }

    @Test
    public void test() {

        PivotTableReportElement table = new PivotTableReportElement();

        DimensionPruner pruner = new DimensionPruner(eventBus, getDispatcher());
        pruner.bind(table);

        table.getFilter().addRestriction(DimensionType.Indicator, NB_MENAGES_INDICATOR_ID);
        table.addColDimension(new Dimension(DimensionType.Indicator));

        eventBus.fireEvent(new ReportChangeEvent(this, table));

        AttributeGroupDimension groupDim = new AttributeGroupDimension(NFI_FUNDING_GROUP_ID);
        table.addColDimension(groupDim);
        eventBus.fireEvent(new ReportChangeEvent(this, table));

        assertTrue(table.getColumnDimensions().contains(groupDim));

        // now remove the indicator and verify that the attribute group has been
        // removed
        table.getFilter().clearRestrictions(DimensionType.Indicator);
        eventBus.fireEvent(new ReportChangeEvent(this, table));

        assertFalse(table.getColumnDimensions().contains(groupDim));
    }

    @Test
    public void testMergedAttributes() {
        PivotTableReportElement table = new PivotTableReportElement();

        DimensionPruner pruner = new DimensionPruner(eventBus, getDispatcher());
        pruner.bind(table);

        table.getFilter().addRestriction(DimensionType.Indicator,
                Arrays.asList(NB_MENAGES_INDICATOR_ID, VOUCHER_INDICATOR_ID));

        table.addColDimension(new Dimension(DimensionType.Indicator));
        eventBus.fireEvent(new ReportChangeEvent(this, table));

        AttributeGroupDimension groupDim = new AttributeGroupDimension(NFI_FUNDING_GROUP_ID);
        table.addColDimension(groupDim);
        eventBus.fireEvent(new ReportChangeEvent(this, table));

        // now remove the first indicator and verify that the attribute group
        // has NOT been removed, because it shares a name with the other attrib group

        table.getFilter().clearRestrictions(DimensionType.Indicator);
        table.getFilter().addRestriction(DimensionType.Indicator, VOUCHER_INDICATOR_ID);
        eventBus.fireEvent(new ReportChangeEvent(this, table));

        assertTrue(table.getColumnDimensions().contains(groupDim));
    }
}
