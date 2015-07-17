package org.activityinfo.core.client.form.tree;

import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.legacy.shared.adapter.ResourceLocatorAdaptor;
import org.activityinfo.server.command.CommandTestCase2;
import org.activityinfo.server.database.OnDataSet;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.activityinfo.core.client.PromiseMatchers.assertResolves;
import static org.junit.Assert.assertThat;


@SuppressWarnings("GwtClientClassFromNonInheritedModule")
@RunWith(InjectionSupport.class)
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class AsyncFormTreeBuilderTest extends CommandTestCase2 {

    @Test
    public void treeResolver() {
        ResourceLocator locator = new ResourceLocatorAdaptor(getDispatcher());
        AsyncFormTreeBuilder treeBuilder = new AsyncFormTreeBuilder(locator);
        ResourceId formClassId = CuidAdapter.activityFormClass(1);
        FormTree tree = assertResolves(treeBuilder.apply(formClassId));

        System.out.println(tree);

        assertThat(tree.getRootFormClasses().keySet(), Matchers.hasItems(formClassId));

    }
}
