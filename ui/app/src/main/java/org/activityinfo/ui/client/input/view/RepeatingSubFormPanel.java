package org.activityinfo.ui.client.input.view;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CssFloatLayoutContainer;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.ui.client.input.viewModel.SubFormInputViewModel;
import org.activityinfo.ui.client.input.viewModel.SubRecordViewModel;

import java.util.HashMap;
import java.util.Map;

/**
 * View containing a list of {@link FormPanel}s, one for each sub record.
 */
public class RepeatingSubFormPanel implements IsWidget {

    private final ResourceId fieldId;
    private InputHandler inputHandler;
    private final FormTree subTree;

    private final FieldSet fieldSet;
    private final CssFloatLayoutContainer container;
    private final CssFloatLayoutContainer recordContainer;
    private final Map<RecordRef, FormPanel> panelMap = new HashMap<>();

    private SubFormInputViewModel viewModel;

    public RepeatingSubFormPanel(FormTree.Node node, FormTree subTree, InputHandler inputHandler) {
        this.subTree = subTree;
        this.fieldId = node.getFieldId();
        this.inputHandler = inputHandler;

        recordContainer = new CssFloatLayoutContainer();

        TextButton addButton = new TextButton(I18N.CONSTANTS.addAnother());
        addButton.addSelectHandler(this::addRecordHandler);

        container = new CssFloatLayoutContainer();
        container.add(recordContainer, new CssFloatLayoutContainer.CssFloatData(1)  );
        container.add(addButton, new CssFloatLayoutContainer.CssFloatData(1, new Margins(20, 0, 5, 0)));

        fieldSet = new FieldSet();
        fieldSet.setHeading(subTree.getRootFormClass().getLabel());
        fieldSet.setCollapsible(true);
        fieldSet.setWidget(container);
    }

    @Override
    public Widget asWidget() {
        return fieldSet;
    }


    public ResourceId getFieldId() {
        return fieldId;
    }

    public void init(SubFormInputViewModel viewModel) {
    }

    public void update(SubFormInputViewModel viewModel) {

        this.viewModel = viewModel;

        // First add any records which are not yet present.
        for (SubRecordViewModel subRecord : viewModel.getSubRecords()) {
            FormPanel subPanel = panelMap.get(subRecord.getRecordRef());
            if(subPanel == null) {
                subPanel = new FormPanel(subTree, subRecord.getRecordRef(), inputHandler);
                subPanel.init(subRecord.getSubFormViewModel());

                recordContainer.add(subPanel, new CssFloatLayoutContainer.CssFloatData(1));
                panelMap.put(subRecord.getRecordRef(), subPanel);
            }
            subPanel.update(subRecord.getSubFormViewModel());
        }

        // Now remove any that have been deleted
        for (FormPanel formPanel : panelMap.values()) {
            if(!viewModel.getSubRecordRefs().contains(formPanel.getRecordRef())) {
                recordContainer.remove(formPanel);
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
        ResourceId subFormId = subTree.getRootFormId();
        ResourceId newSubRecordId = ResourceId.generateId();

        inputHandler.addSubRecord(new RecordRef(subFormId, newSubRecordId));
    }

}
