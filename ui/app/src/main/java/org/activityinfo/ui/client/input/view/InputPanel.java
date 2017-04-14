package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

public class InputPanel implements IsWidget {

    private final Observable<FormTree> formTree;

    private FieldPanel fieldPanel = null;

    private VerticalLayoutContainer container;

    public InputPanel(FormStore formStore, ResourceId formId) {
        this.formTree = formStore.getFormTree(formId);
        this.formTree.subscribe(this::onTreeChanged);

        container = new VerticalLayoutContainer();
    }

    private void onTreeChanged(Observable<FormTree> formTree) {
        if(formTree.isLoading()) {
          //  container.mask(I18N.CONSTANTS.loading());
        } else if(fieldPanel == null) {
            fieldPanel = new FieldPanel(formTree.get());
            container.add(fieldPanel, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
            container.forceLayout();
        } else {
            // Alert the user that the schema has been updated.

        }
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
