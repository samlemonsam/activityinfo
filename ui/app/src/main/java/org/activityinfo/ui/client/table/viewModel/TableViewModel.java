package org.activityinfo.ui.client.table.viewModel;

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Model's the user's selection of columns
 */
public class TableViewModel {

    private final FormStore formStore;
    private ResourceId formId;
    private Observable<FormTree> formTree;
    private Observable<EffectiveTableModel> effectiveTable;

    public TableViewModel(final FormStore formStore, ResourceId formId) {
        this.formId = formId;
        this.formStore = formStore;
        this.formTree = formStore.getFormTree(formId);
        this.effectiveTable = formTree.transform(tree -> new EffectiveTableModel(formStore, tree));
    }

    public Observable<EffectiveTableModel> getEffectiveTable() {
        return effectiveTable;
    }

    public ResourceId getFormId() {
        return formId;
    }

    public Observable<FormTree> getFormTree() {
        return formTree;
    }

    public FormStore getFormStore() {
        return formStore;
    }
}
