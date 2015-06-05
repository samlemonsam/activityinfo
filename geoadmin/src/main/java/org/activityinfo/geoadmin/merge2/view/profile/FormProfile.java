package org.activityinfo.geoadmin.merge2.view.profile;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPair;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
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
    private final Map<ResourceId, Integer> idMap = new HashMap<>();

    public FormProfile(FormTree formTree, ColumnSet columnSet) {
        this.formTree = formTree;
        this.columnSet = columnSet;
        for (FormTree.Node node : formTree.getLeaves()) {
            fields.add(new FieldProfile(node, columnSet.getColumnView(node.getFieldId().asString())));
        }

        this.id = columnSet.getColumnView(ID_COLUMN);
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            idMap.put(ResourceId.valueOf(id.getString(i)), i);
        }
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

                for (FormTree.Node node : tree.getLeaves()) {
                    if(node.getType() instanceof TextType) {
                        queryModel
                                .selectField(node.getFieldId())
                                .as(node.getFieldId().asString());
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

    public void dump(int index) {
        System.out.println(toString(index));
    }

    public String toString(int index) {
        StringBuilder s = new StringBuilder();
        for (FieldProfile field : fields) {
            if(field.getView() != null) {
                s.append("[").append(field.getLabel()).append(" = ").append(field.getView().getString(index)).append("]");
            }
        }
        return s.toString();
    }

//    /**
//     * Create new, synthetic FormProfile that contains distinct 
//     * @param fieldPath
//     * @return
//     */
//    public FormProfile distinct(List<FieldPath> fieldPath) {
//        
//        
//        
//    }
}
