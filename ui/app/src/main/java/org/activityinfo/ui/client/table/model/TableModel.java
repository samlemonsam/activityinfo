package org.activityinfo.ui.client.table.model;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Model's the user's selection of columns
 */
public class TableModel {

    private ResourceId formId;
    private Observable<FormTree> formTree;
    private Observable<EffectiveTableModel> effectiveTable;

    public TableModel(final FormStore service, ResourceId formId) {
        this.formId = formId;
        this.formTree = service.getFormTree(formId);
        this.effectiveTable = formTree.transform(tree -> new EffectiveTableModel(service, tree));
    }

    public Observable<EffectiveTableModel> getEffectiveTable() {
        return effectiveTable;
    }
}
