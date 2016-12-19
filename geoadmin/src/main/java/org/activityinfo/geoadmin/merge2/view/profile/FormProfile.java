package org.activityinfo.geoadmin.merge2.view.profile;

import com.google.common.base.Function;
import org.activityinfo.model.expr.FunctionCallNode;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.expr.functions.BoundingBoxFunction;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnType;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;
import org.activityinfo.store.ResourceStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a Form and its fields, and the values associated with those fields.
 */
public class FormProfile {

    public static final String ID_COLUMN = "_id";

    private FormTree formTree;
    private final ColumnSet columnSet;
    private final ColumnView id;
    private final List<FieldProfile> fields = new ArrayList<>();
    private final List<FieldProfile> geometryFields = new ArrayList<>();
    private final Map<ResourceId, Integer> idMap = new HashMap<>();

    public FormProfile(FormTree formTree, ColumnSet columnSet) {
        this.formTree = formTree;
        this.columnSet = columnSet;
        for (FormTree.Node node : formTree.getLeaves()) {
            ColumnView view = columnSet.getColumnView(node.getFieldId().asString());
            if(view != null && !isDuplicateColumn(view)) {
                fields.add(new FieldProfile(node, view));
            }
        }
        
        this.id = columnSet.getColumnView(ID_COLUMN);
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            idMap.put(ResourceId.valueOf(id.getString(i)), i);
        }
    }

    /**
     * Checks to see if the given column is an exact duplicate of an existing column in the
     * {@code fields} array. This is important to check because identical duplicates of columns will
     * lead to degenerate results in identifying column pairs for matching.
     * 
     * @param view
     * @return
     */
    private boolean isDuplicateColumn(ColumnView view) {
        if(view.getType() == ColumnType.STRING) {
            for (FieldProfile existingField : fields) {
                if(existingField.hasIdenticalContents(view)) {
                    return true;
                }
            }
        }
        return false;
    }

    public FormTree getFormTree() {
        return formTree;
    }

    public ResourceId getRowId(int rowIndex) {
        return ResourceId.valueOf(id.getString(rowIndex));
    }

    public List<FieldProfile> getFields() {
        return fields;
    }

    public String getLabel() {
        return formTree.getRootFormClass().getLabel();
    }

    public int getRowCount() {
        return columnSet.getNumRows();
    }

    public int indexOf(ResourceId resourceId) {
        return idMap.get(resourceId);
    }

    public static Observable<FormProfile> profile(final ResourceStore resourceStore, ResourceId formClassId) {  
        return profile(resourceStore, resourceStore.getFormTree(formClassId));
    }

    public static Observable<FormProfile> profile(final ResourceStore resourceStore, final Observable<FormTree> formTree) {
        Observable<ColumnSet> columnSet = formTree.join(new Function<FormTree, Observable<ColumnSet>>() {
            @Override
            public Observable<ColumnSet> apply(FormTree tree) {
                QueryModel queryModel = new QueryModel(tree.getRootFormClass().getId());
                queryModel.selectResourceId().as(ID_COLUMN);

                

                // Add all reachable text fields as matchable fields
                for (FormTree.Node node : tree.getLeaves()) {
                    if(node.getType() instanceof TextType) {
                        queryModel
                                .selectField(node.getFieldId())
                                .as(node.getFieldId().asString());
                    }
                }

                // Add only geometry from the root form
                for (FormTree.Node node : tree.getRootFields()) {
                    if(node.getType() instanceof GeoAreaType) {
                        SymbolExpr bounds = new SymbolExpr(node.getFieldId());
                        queryModel.selectExpr(new FunctionCallNode(BoundingBoxFunction.XMIN, bounds)).as("_xmin");
                        queryModel.selectExpr(new FunctionCallNode(BoundingBoxFunction.XMAX, bounds)).as("_xmax");
                        queryModel.selectExpr(new FunctionCallNode(BoundingBoxFunction.YMIN, bounds)).as("_ymin");
                        queryModel.selectExpr(new FunctionCallNode(BoundingBoxFunction.YMAX, bounds)).as("_ymax");
                    }
                }
                return resourceStore.queryColumns(queryModel);
            }
        });

        return Observable.transform(formTree, columnSet, new BiFunction<FormTree, ColumnSet, FormProfile>() {
            @Override
            public FormProfile apply(FormTree tree, ColumnSet columnSet) {
                return new FormProfile(formTree.get(), columnSet);
            }
        });
    }

    public FieldProfile getField(String fieldName) {
        for (FieldProfile field : fields) {
            if(field.getLabel().equals(fieldName)) {
                return field;
            }
        }
        throw new IllegalArgumentException("No such field: " + fieldName);
    }


    public FieldProfile getField(ResourceId id) {
        for (FieldProfile field : fields) {
            if(field.getId().equals(id)) {
                return field;
            }
        }
        throw new IllegalArgumentException("No such field: " + id);
    }

    public void dump(int index) {
        System.out.println(toString(index));
    }

    public String toString(int index) {
        StringBuilder s = new StringBuilder();
        for (FieldProfile field : fields) {
            if(field.getView() != null) {
                s.append("[").append(field.getLabel()).append(" = ").append(field.getView().get(index)).append("]");
            }
        }
        return s.toString();
    }

}
