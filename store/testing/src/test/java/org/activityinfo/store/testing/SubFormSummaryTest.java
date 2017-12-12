package org.activityinfo.store.testing;

import com.google.common.collect.Iterables;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NodeMatch;
import org.activityinfo.store.query.shared.plan.FieldPlanNode;
import org.activityinfo.store.query.shared.NodeMatcher;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.query.shared.join.JoinNode;
import org.activityinfo.store.query.shared.plan.PlanNode;
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
        TestingCatalog catalog = new TestingCatalog();
        ClinicForm clinicForm = catalog.getClinicForm();

        FormTree formTree = catalog.getFormTree(clinicForm.getFormId());
        NodeMatcher nodeMatcher = new NodeMatcher(formTree);
        Collection<NodeMatch> nodeMatches = nodeMatcher.resolveSymbol(new SymbolExpr("NUM_CONSULT"));

        assertThat(nodeMatches, hasSize(1));

        NodeMatch nodeMatch = Iterables.getOnlyElement(nodeMatches);
        JoinNode joinNode = Iterables.getOnlyElement(nodeMatch.getJoins());


    }


    @Test
    public void minMaxFunction() {
        TestingCatalog catalog = new TestingCatalog();
        ClinicForm clinicForm = catalog.getClinicForm();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormScanCache(), new NullFormSupervisor());

        QueryModel queryModel = new QueryModel(clinicForm.getFormId());
        queryModel.selectResourceId().as("id");
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
