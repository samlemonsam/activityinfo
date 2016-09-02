package org.activityinfo.ui.client.pageView.formClass;

import com.google.common.base.Function;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.formTree.AsyncFormTreeBuilder;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTableView;
import org.activityinfo.ui.client.widget.DisplayWidget;

import java.util.List;

/**
 * Presents the instances of this form class as table
 */
public class TableTab implements DisplayWidget<ResourceId> {

    private InstanceTableView tableView;

    private FormTree formTree;

    private List<FieldColumn> columns;
    private ResourceLocator resourceLocator;

    public TableTab(ResourceLocator resourceLocator, StateProvider stateProvider) {
        this.resourceLocator = resourceLocator;
        this.tableView = new InstanceTableView(resourceLocator, stateProvider);
    }

    @Override
    public Promise<Void> show(final ResourceId resourceId) {
        return new AsyncFormTreeBuilder(resourceLocator)
                .apply(resourceId)
                .join(new Function<FormTree, Promise<Void>>() {
                    @Override
                    public Promise<Void> apply(FormTree input) {
                        formTree = input;

                        columns = FieldColumn.create(formTree.getColumnNodes());

                        tableView.setRootFormClass(formTree.getRootFormClass());
                        tableView.setColumns(columns);

                        return Promise.done();
                    }
                });
    }

    @Override
    public Widget asWidget() {
        return tableView.asWidget();
    }

}
