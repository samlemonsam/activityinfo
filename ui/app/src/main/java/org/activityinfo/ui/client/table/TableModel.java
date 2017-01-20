package org.activityinfo.ui.client.table;

import com.google.common.base.Function;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.data.FormService;

/**
 * Model's the user's selection of columns
 */
public class TableModel {

    private ResourceId formId;
    private Observable<FormTree> formTree;
    private Observable<EffectiveTableModel> effectiveTable;

    public TableModel(final FormService service, ResourceId formId) {
        this.formId = formId;
        this.formTree = service.getFormTree(formId);
        this.effectiveTable = formTree.transform(new Function<FormTree, EffectiveTableModel>() {
            @Override
            public EffectiveTableModel apply(FormTree formTree) {
                return new EffectiveTableModel(service, formTree);
            }
        });
    }

    public Observable<EffectiveTableModel> getEffectiveTable() {
        return effectiveTable;
    }
}
