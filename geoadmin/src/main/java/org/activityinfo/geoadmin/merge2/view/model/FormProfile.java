package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.state.ResourceStore;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.observable.Observable;
import org.activityinfo.promise.BiFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a Form and its fields, and the values associated with those fields.
 */
public class FormProfile {

    public static final String ID_COLUMN = "_id";

    private FormTree formTree;
    private final ColumnSet columnSet;
    private final List<FieldProfile> fields = new ArrayList<>();

    public FormProfile(FormTree formTree, ColumnSet columnSet) {
        this.formTree = formTree;
        this.columnSet = columnSet;
        
        for (FormTree.Node node : formTree.getLeaves()) {
            fields.add(new FieldProfile(node, columnSet.getColumnView(node.getFieldId().asString())));
        }
        
    }

    public List<FieldProfile> getFields() {
        return fields;
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


    public String getLabel() {
        return formTree.getRootFormClass().getLabel();
    }

    public int getRowCount() {
        return columnSet.getNumRows();
    }
}
