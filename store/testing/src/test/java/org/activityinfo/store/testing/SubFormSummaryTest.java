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

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NodeMatch;
import org.activityinfo.store.query.shared.NodeMatcher;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.query.shared.join.JoinNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SubFormSummaryTest {

    @BeforeClass
    public static void setupLocale() {
        LocaleProxy.initialize();
    }

    @Test
    public void matchNodes() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ClinicForm clinicForm = catalog.getClinicForm();

        FormTree formTree = catalog.getFormTree(clinicForm.getFormId());
        NodeMatcher nodeMatcher = new NodeMatcher(formTree);
        Collection<NodeMatch> nodeMatches = nodeMatcher.resolveSymbol(new SymbolNode("NUM_CONSULT"));

        assertThat(nodeMatches, hasSize(1));

        NodeMatch nodeMatch = Iterables.getOnlyElement(nodeMatches);
        JoinNode joinNode = Iterables.getOnlyElement(nodeMatch.getJoins());


    }


    @Test
    public void minMaxFunction() {
        TestingStorageProvider catalog = new TestingStorageProvider();
        ClinicForm clinicForm = catalog.getClinicForm();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(clinicForm.getFormId());
        queryModel.selectRecordId().as("id");
        queryModel.selectExpr("MIN(NUM_CONSULT)").as("min");
        queryModel.selectExpr("MAX(NUM_CONSULT)").as("max");

        ColumnSet columnSet = builder.build(queryModel);
        ColumnView id = columnSet.getColumnView("id");
        ColumnView min = columnSet.getColumnView("min");
        ColumnView max = columnSet.getColumnView("max");

        System.out.println(columnSet.getColumnView("id"));
        System.out.println(min);
        System.out.println(max);

        assertThat(id.getString(0), equalTo("c0"));
        assertThat(min.getDouble(0), equalTo(56.0));
        assertThat(max.getDouble(0), equalTo(247.0));
    }
}
