package org.activityinfo.ui.client.pageView.formClass;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.client.form.tree.AsyncFormTreeBuilder;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.table.FieldColumn;
import org.activityinfo.ui.client.component.table.InstanceTableView;
import org.activityinfo.ui.client.widget.DisplayWidget;

import java.util.List;
import java.util.Map;

/**
 * Presents the instances of this form class as table
 */
public class TableTab implements DisplayWidget<FormInstance> {

    private InstanceTableView tableView;

    private FormTree formTree;
    private Map<ResourceId, FieldColumn> columnMap;

    private List<FieldColumn> columns;
    private ResourceLocator resourceLocator;

    public TableTab(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;
        this.tableView = new InstanceTableView(resourceLocator);
    }

    @Override
    public Promise<Void> show(FormInstance instance) {
        return new AsyncFormTreeBuilder(resourceLocator)
        .apply(instance.getId())
        .join(new Function<FormTree, Promise<Void>>() {
            @Override
            public Promise<Void> apply(FormTree input) {
                formTree = input;
                enumerateColumns();

                final Map<ResourceId, FormClass> rootFormClasses = formTree.getRootFormClasses();

                tableView.setRootFormClasses(rootFormClasses.values());
                tableView.setCriteria(ClassCriteria.union(rootFormClasses.keySet()));
                tableView.setColumns(columns);

                return Promise.done();
            }
        });
    }

    @Override
    public Widget asWidget() {
        return tableView.asWidget();
    }

    /**
     * @return a list of possible FieldColumns to display
     */
    private void enumerateColumns() {

        columnMap = Maps.newHashMap();
        columns = Lists.newArrayList();

        enumerateColumns(formTree.getRootFields());
    }

    private void enumerateColumns(List<FormTree.Node> fields) {
        for (FormTree.Node node : fields) {
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
