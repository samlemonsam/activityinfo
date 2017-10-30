package org.activityinfo.ui.client.input.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.theme.triton.custom.client.toolbar.TritonToolBarAppearance;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.view.period.Monthly;
import org.activityinfo.ui.client.input.viewModel.KeyedSubFormViewModel;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * A sub form panel that is keyed by a date field.
 *
 * <p>For example, monthly, weekly, etc. The keys are show in a tab bar and
 * only one form is shown at a time.</p>
 */
public class KeyedSubFormPanel implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(KeyedSubFormPanel.class.getName());

    private final ResourceId fieldId;
    private final ResourceId subFormId;

    private final Monthly selector;
    private final InputHandler inputHandler;
    private final FormPanel formPanel;
    private final ContentPanel contentPanel;

    private RecordRef activeRef;

    public KeyedSubFormPanel(RecordRef parentRef, FormSource formSource, FormTree.Node node,
                             FormTree subTree, InputHandler inputHandler) {

        this.fieldId = node.getFieldId();
        this.subFormId = subTree.getRootFormId();
        this.inputHandler = inputHandler;

        selector = new Monthly(parentRef);
        selector.addSelectionHandler(this::onPeriodSelected);
        ToolBar toolBar = new ToolBar(new KeyedSubFormBarAppearance());
        for (Component component : selector.getToolBarItems()) {
            toolBar.add(component);
        }

        formPanel = new FormPanel(formSource, subTree,
                new RecordRef(subTree.getRootFormId(), ResourceId.generateId()), inputHandler);
        formPanel.setBorders(false);

        VerticalLayoutContainer vlc = new VerticalLayoutContainer();
        vlc.add(toolBar, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
        vlc.add(formPanel, new VerticalLayoutContainer.VerticalLayoutData(1, 1,
                new Margins(0, 5, 0, 5)));

        contentPanel = new ContentPanel();
        contentPanel.setHeading(subTree.getRootFormClass().getLabel());
        contentPanel.add(vlc);
        contentPanel.setBorders(true);
    }

    private void onPeriodSelected(SelectionEvent<ResourceId> event) {
        LOGGER.info("subFormId = " + event.getSelectedItem());

        RecordRef newActiveRef = new RecordRef(subFormId, event.getSelectedItem());
        inputHandler.changeActiveSubRecord(fieldId, newActiveRef);
    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public void update(KeyedSubFormViewModel viewModel) {

        LOGGER.info("activeRef = " + viewModel.getActiveRecordRef());

        if(!Objects.equals(activeRef, viewModel.getActiveRecordRef())) {
            selector.update(viewModel);
            formPanel.init(viewModel.getSubRecord().getSubFormViewModel());
            activeRef = viewModel.getActiveRecordRef();
        }

        selector.update(viewModel);
        formPanel.update(viewModel.getSubRecord().getSubFormViewModel());
    }

    private class KeyedSubFormBarAppearance extends TritonToolBarAppearance {

        @Override
        public String toolBarClassName() {
            return super.toolBarClassName() + " " + InputResources.INSTANCE.style().periodToolBar();
        }
    }
}
