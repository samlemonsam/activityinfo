package org.activityinfo.geoadmin.merge;

import org.activityinfo.geoadmin.model.ActivityInfoClient;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a form that is the target of updates in a merge operation.
 */
public class MergeFormViewModel {

    private FormTree tree;
    private ColumnSet columnSet;
    private List<MergeColumn> textFields = new ArrayList<>();

    public void build(ActivityInfoClient client, ResourceId resourceId) {
        this.tree = client.getFormTree(resourceId);
        this.columnSet = client.queryColumns(queryModel(resourceId));
        buildColumns();
    }


    public void build(CollectionCatalog catalog, ResourceId resourceId) {
        FormTreeBuilder treeBuilder = new FormTreeBuilder(catalog);
        this.tree = treeBuilder.queryTree(resourceId);

        ColumnSetBuilder columnSetBuilder = new ColumnSetBuilder(catalog, ColumnCache.NULL);
        this.columnSet = columnSetBuilder.build(queryModel(resourceId));

        buildColumns();
    }


    private QueryModel queryModel(ResourceId resourceId) {
        QueryModel queryModel = new QueryModel(resourceId);
        for (FormTree.Node node : this.tree.getLeaves()) {
            if(node.getType() instanceof TextType) {
                queryModel
                        .selectField(node.getFieldId())
                        .as(node.getFieldId().asString());
            }
        }
        return queryModel;
    }


    private void buildColumns() {
        for (FormTree.Node node : tree.getLeaves()) {
            if (node.getType() instanceof TextType) {
                textFields.add(new MergeColumn(node, columnSet.getColumnView(node.getFieldId().asString())));
            }
        }
    }


    public ResourceId getClassId() {
        return tree.getRootFormClasses().values().iterator().next().getId();
    }

    public FormTree getTree() {
        return tree;
    }

    public List<MergeColumn> getTextFields() {
        return textFields;
    }
}
