package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;
import org.activityinfo.ui.client.table.viewModel.TableViewModel;

/**
 * Hosts subform tabs
 */
public class SubFormPane extends TabPanel {

    private TableViewModel viewModel;
    private Subscription subscription;

    public SubFormPane(TableViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        subscription = viewModel.getFormTree().subscribe(this::onFormTreeChanged);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        subscription.unsubscribe();
    }

    private void onFormTreeChanged(Observable<FormTree> formTree) {
        if(formTree.isLoaded()) {
            for (FormTree.Node node : formTree.get().getRootFields()) {
                if(node.isSubForm()) {
                    SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
                    FormClass subForm = formTree.get().getFormClass(subFormType.getClassId());
                    add(new SubFormGrid(viewModel, subForm.getId()),
                            new TabItemConfig(subForm.getLabel()));
                }
            }

        }
    }
}
