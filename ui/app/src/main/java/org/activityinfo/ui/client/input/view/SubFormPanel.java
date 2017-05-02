package org.activityinfo.ui.client.input.view;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.viewModel.SubFormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.SubRecordViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains repeating
 */
public class SubFormPanel implements IsWidget {

    private final ResourceId fieldId;
    private InputHandler inputHandler;
    private final FormTree subTree;

    private final FlowLayoutContainer container;
    private final FlowLayoutContainer recordContainer;
    private final Map<RecordRef, FieldPanel> panelMap = new HashMap<>();

    private SubFormInputViewModel viewModel;

    public SubFormPanel(FormTree.Node node, FormTree subTree, InputHandler inputHandler) {
        this.subTree = subTree;
        this.fieldId = node.getFieldId();
        this.inputHandler = inputHandler;

        recordContainer = new FlowLayoutContainer();

        TextButton addButton = new TextButton(I18N.CONSTANTS.addAnother());
        addButton.addSelectHandler(this::addRecordHandler);

        container = new FlowLayoutContainer();
        container.add(recordContainer);
        container.add(addButton);
    }


    public ResourceId getFieldId() {
        return fieldId;
    }

    public void update(SubFormInputViewModel viewModel) {

        this.viewModel = viewModel;

        // First add any records which are not yet present.
        for (SubRecordViewModel subRecord : viewModel.getSubRecords()) {
            FieldPanel subPanel = panelMap.get(subRecord.getRecordRef());
            if(subPanel == null) {
                subPanel = new FieldPanel(subTree, subRecord.getRecordRef(), inputHandler);
                recordContainer.add(subPanel);
                panelMap.put(subRecord.getRecordRef(), subPanel);
            }
            subPanel.update(subRecord.getSubFormViewModel());
        }

        // Now remove any that have been deleted
        for (FieldPanel fieldPanel : panelMap.values()) {
            if(!viewModel.getSubRecordRefs().contains(fieldPanel.getRecordRef())) {
                recordContainer.remove(fieldPanel);
            }
        }
    }


    private void addRecordHandler(SelectEvent event) {

        // If we have a placeholder, then add it first, otherwise it will
        // disappear
        Optional<SubRecordViewModel> placeholder = viewModel.getPlaceholder();
        if(placeholder.isPresent()) {
            inputHandler.addSubRecord(placeholder.get().getRecordRef());
        }

        // Add a new sub record with unique ID
        ResourceId subFormId = subTree.getRootFormClass().getId();
        ResourceId newSubRecordId = ResourceId.generateId();

        inputHandler.addSubRecord(new RecordRef(subFormId, newSubRecordId));
    }

    @Override
    public Widget asWidget() {
        return container;
    }
}
