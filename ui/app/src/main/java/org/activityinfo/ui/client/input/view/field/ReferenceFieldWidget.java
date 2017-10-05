package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoice;
import org.activityinfo.ui.client.lookup.view.LevelWidget;
import org.activityinfo.ui.client.lookup.viewModel.LookupKeyViewModel;
import org.activityinfo.ui.client.lookup.viewModel.LookupViewModel;

import java.util.ArrayList;
import java.util.List;

public class ReferenceFieldWidget implements FieldWidget {


    private final LookupViewModel viewModel;
    private final List<LevelWidget> levelWidgets = new ArrayList<>();
    private final Widget widget;
    private FieldUpdater fieldUpdater;

    public ReferenceFieldWidget(FormSource formSource, FormTree formTree, FormField field, FieldUpdater fieldUpdater) {
        this.fieldUpdater = fieldUpdater;
        this.viewModel = new LookupViewModel(formSource, formTree, (ReferenceType) field.getType());

        for (LookupKeyViewModel level : viewModel.getLookupKeys()) {
            LevelWidget widget = new LevelWidget(viewModel, level);
            if(level.isLeaf()) {
                widget.addSelectionHandler(this::onSelection);
            }
            levelWidgets.add(widget);
        }

        if(viewModel.getLookupKeys().size() == 1) {
            // If we have a single lookup key, then just show a single combobox
            widget = new LevelWidget(viewModel, viewModel.getLookupKeys().get(0)).getComboBox();

        } else {
            // Otherwise we need labeled rows of comboboxes
            CssFloatLayoutContainer panel = new CssFloatLayoutContainer();
            for (LevelWidget levelWidget : levelWidgets) {
                panel.add(levelWidget, new CssFloatLayoutContainer.CssFloatData(1, new Margins(5, 0, 5, 0)));
            }
            widget = panel;
        }
    }

    private void onSelection(SelectionEvent<ReferenceChoice> event) {
        fieldUpdater.update(new FieldInput(new ReferenceValue(event.getSelectedItem().getRef())));
    }

    @Override
    public void init(FieldValue value) {
        ReferenceValue referenceValue = (ReferenceValue) value;
        RecordRef recordRef = referenceValue.getOnlyReference();
        if(recordRef.getFormId().equals(viewModel.getReferencedFormId())) {
            viewModel.setInitialSelection(recordRef);
        }
    }

    @Override
    public void setRelevant(boolean relevant) {
        for (LevelWidget levelWidget : levelWidgets) {
            levelWidget.setRelevant(relevant);
        }
    }

    @Override
    public Widget asWidget() {
        return widget;
    }
}
