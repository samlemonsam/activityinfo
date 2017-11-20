package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Subscription;

import java.util.HashMap;
import java.util.Map;

/**
 * Hosts sub form tabs
 */
public class SubFormPane extends TabPanel {

    private TableViewModel viewModel;
    private Subscription subscription;

    private final Map<ResourceId, TabItemConfig> tabs = new HashMap<>();

    public SubFormPane(TableViewModel viewModel, FormTree formTree) {
        this.viewModel = viewModel;
        updateTabs(formTree);
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
            updateTabs(formTree.get());
        }
    }

    private void updateTabs(FormTree formTree) {
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                addOrUpdateTab(formTree, node);
            }
        }
    }

    private void addOrUpdateTab(FormTree formTree, FormTree.Node node) {

        TabItemConfig tabItemConfig = tabs.get(node.getFieldId());
        if(tabItemConfig == null) {
            SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
            FormClass subForm = formTree.getFormClass(subFormType.getClassId());
            TabItemConfig config = new TabItemConfig(subForm.getLabel());
            tabs.put(node.getFieldId(), config);
            add(new SubFormGrid(viewModel, subForm.getId()), config);
        }
    }

}
