package org.activityinfo.ui.client.pageView.formClass;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.formTree.AsyncFormTreeBuilder;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTableView;
import org.activityinfo.ui.client.widget.DisplayWidget;

import java.util.List;
import java.util.Map;

/**
 * Presents the instances of this form class as table
 */
public class TableTab implements DisplayWidget<ResourceId> {

    private InstanceTableView tableView;

    private FormTree formTree;
    private Map<ResourceId, FieldColumn> columnMap;

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
                        enumerateColumns();

                        tableView.setRootFormClass(formTree.getRootFormClass());
                        tableView.setCriteria(ClassCriteria.union(Sets.newHashSet(resourceId)));
                        tableView.setColumns(columns);

                        return Promise.done();
                    }
                });
    }

    @Override
    public Widget asWidget() {
        return tableView.asWidget();
    }

    private void enumerateColumns() {

        columnMap = Maps.newHashMap();
        columns = Lists.newArrayList();

        enumerateColumns(formTree.getRootFields());
    }

    private void enumerateColumns(List<FormTree.Node> fields) {
        for (FormTree.Node node : fields) {

            if (node.getType() instanceof SubFormReferenceType) { // skip subForm fields
                continue;
            }

            if (node.isReference()) {
                enumerateColumns(node.getChildren());
            } else {
                if (columnMap.containsKey(node.getFieldId())) {
                    columnMap.get(node.getFieldId()).addFieldPath(node.getPath());
                } else {
                    FieldColumn col = new FieldColumn(node);
                    columnMap.put(node.getFieldId(), col);
                    columns.add(col);
                }
            }
        }
    }

}
