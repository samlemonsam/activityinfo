package org.activityinfo.ui.client.input.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.theme.triton.custom.client.toolbar.TritonToolBarAppearance;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.time.PeriodValue;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.view.field.*;
import org.activityinfo.ui.client.input.viewModel.SubFormViewModel;

import java.util.logging.Logger;

/**
 * A sub form panel that is keyed by a date field.
 *
 * <p>For example, monthly, weekly, etc. The keys are show in a tab bar and
 * only one form is shown at a time.</p>
 */
public class KeyedSubFormPanel implements IsWidget {

    private static final Logger LOGGER = Logger.getLogger(KeyedSubFormPanel.class.getName());

    private RecordRef parentRef;
    private final ResourceId fieldId;
    private final ResourceId subFormId;

    private final PeriodFieldWidget selector;

    private final FormPanel formPanel;
    private final ContentPanel contentPanel;
    private final InputHandler inputHandler;

    private SubFormViewModel viewModel;

    public KeyedSubFormPanel(RecordRef parentRef, FormSource formSource, FormTree.Node node,
                             FormTree subTree, InputHandler inputHandler) {

        this.fieldId = node.getFieldId();
        this.subFormId = subTree.getRootFormId();
        this.parentRef = parentRef;
        this.inputHandler = inputHandler;

        selector = createSelector(subTree.getRootFormClass().getSubFormKind(), this::onPeriodSelected);

        TextButton previousButton = new TextButton();
        previousButton.setText("<");
        previousButton.addSelectHandler(this::onPreviousPeriod);

        TextButton nextButton = new TextButton();
        nextButton.setText(">");
        nextButton.addSelectHandler(this::onNextPeriod);

        ToolBar toolBar = new ToolBar(new KeyedSubFormBarAppearance());
        toolBar.add(previousButton);
        for (Component component : selector.asToolBarItems()) {
            toolBar.add(component);
        }
        toolBar.add(nextButton);

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


    private PeriodFieldWidget createSelector(SubFormKind subFormKind, FieldUpdater updater) {
        switch (subFormKind) {
            case MONTHLY:
                return new MonthWidget(updater);
            case BIWEEKLY:
                return new FortnightWidget(updater);
            case WEEKLY:
                return new WeekWidget(updater);
            case DAILY:
                return new LocalDateWidget(updater);
        }
        throw new UnsupportedOperationException("kind: " + subFormKind);
    }


    private void onPreviousPeriod(SelectEvent event) {
        changeActivePeriod(viewModel.getActivePeriod().previous());
    }


    private void onNextPeriod(SelectEvent event) {
        changeActivePeriod(viewModel.getActivePeriod().next());
    }

    private void onPeriodSelected(FieldInput input) {
        if(input.getState() == FieldInput.State.VALID) {
            PeriodValue periodValue = (PeriodValue) input.getValue();
            changeActivePeriod(periodValue);
        }
    }

    private void changeActivePeriod(PeriodValue periodValue) {

        if(!canChangePeriod()) {
            selector.init(viewModel.getActivePeriod());
            return;
        }

        inputHandler.changeActiveSubRecord(fieldId,
                new RecordRef(subFormId,
                     ResourceId.periodSubRecordId(parentRef, periodValue)));
    }


    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    public ResourceId getFieldId() {
        return fieldId;
    }

    public void update(SubFormViewModel viewModel) {

        LOGGER.info("activeRef = " + viewModel.getActiveRecordRef());

        if(this.viewModel == null ||
            !this.viewModel.getActiveRecordRef().equals(viewModel.getActiveRecordRef())) {

            selector.init(viewModel.getActivePeriod());
            formPanel.init(viewModel.getActiveSubViewModel());
        }

        formPanel.update(viewModel.getActiveSubViewModel());

        this.viewModel = viewModel;
    }

    private boolean canChangePeriod() {
        if(viewModel.getActiveSubViewModel().isDirty() &&
          !viewModel.getActiveSubViewModel().isValid()) {

            MessageBox box = new MessageBox(I18N.CONSTANTS.error(), I18N.CONSTANTS.pleaseFillInAllRequiredFields());
            box.setModal(true);
            box.show();
            return false;
        }
        return true;
    }

    private class KeyedSubFormBarAppearance extends TritonToolBarAppearance {

        @Override
        public String toolBarClassName() {
            return super.toolBarClassName() + " " + InputResources.INSTANCE.style().periodToolBar();
        }
    }

}
