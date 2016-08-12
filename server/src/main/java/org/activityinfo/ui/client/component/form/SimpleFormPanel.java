package org.activityinfo.ui.client.component.form;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.BuiltinFields;
import org.activityinfo.model.lock.LockEvaluator;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FieldWidgetMode;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.form.subform.PeriodSubFormPanel;
import org.activityinfo.ui.client.component.form.subform.SubFormPanel;
import org.activityinfo.ui.client.component.form.subform.Tab;
import org.activityinfo.ui.client.widget.ButtonWithIcon;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.icons.Icons;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Displays a simple view of the form, where users can edit instances
 */
public class SimpleFormPanel implements DisplayWidget<FormInstance>, FormWidgetCreator.FieldUpdated {

    private final FlowPanel panel;
    private final ScrollPanel scrollPanel;
    private final boolean withScroll;

    private final FormModel model;
    private final FormWidgetCreator widgetCreator;
    private final FormActions formActions;
    private final ResourceLocator locator;
    private final RelevanceHandler relevanceHandler;
    private final PanelFiller panelFiller;
    private Optional<Button> deleteButton = Optional.absent();

    public SimpleFormPanel(ResourceLocator resourceLocator, StateProvider stateProvider) {
        this(resourceLocator, stateProvider, new VerticalFieldContainer.Factory(),
                new FormFieldWidgetFactory(resourceLocator, FieldWidgetMode.NORMAL));
    }

    public SimpleFormPanel(ResourceLocator locator, StateProvider stateProvider, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory) {
        this(locator, stateProvider, containerFactory, widgetFactory, true);
    }

    public SimpleFormPanel(ResourceLocator locator, StateProvider stateProvider, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory, boolean withScroll) {
        FormPanelStyles.INSTANCE.ensureInjected();

        Preconditions.checkNotNull(locator);
        Preconditions.checkNotNull(containerFactory);
        Preconditions.checkNotNull(widgetFactory);

        this.locator = locator;

        this.model = new FormModel(locator, stateProvider);
        this.withScroll = withScroll;
        this.relevanceHandler = new RelevanceHandler(this);
        this.widgetCreator = new FormWidgetCreator(model, containerFactory, widgetFactory);

        this.panel = new FlowPanel();
        this.panel.setStyleName(FormPanelStyles.INSTANCE.formPanel());
        this.panel.addStyleName("hide-button-on-over");
        this.scrollPanel = new ScrollPanel(panel);
        this.formActions = new FormActions(locator, model);
        this.panelFiller = new PanelFiller(panel, model, widgetCreator, relevanceHandler);
    }

    public Button addDeleteButton() { // used for collection subforms
        deleteButton = Optional.of(new Button());
        deleteButton.get().setHTML(ButtonWithIcon.TEMPLATES.withIcon(Icons.INSTANCE.remove()));
        deleteButton.get().setStyleName("btn btn-default btn-xs pull-right");

        this.panel.add(deleteButton.get());
        return deleteButton.get();
    }

    public Optional<Button> getDeleteButton() {
        return deleteButton;
    }

    public FormModel getModel() {
        return model;
    }

    public Promise<Void> show(final Resource instance) {
        return show(FormInstance.fromResource(instance));
    }

    @Override
    public Promise<Void> show(final FormInstance instance) {
        model.setWorkingRootInstance(instance);

        final Promise<Void> result = new Promise<>();

        model.loadFormClassWithDependentSubForms(instance.getClassId()).then(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                buildForm(model.getRootFormClass()).then(new Function<Void, Object>() {
                    @Override
                    public Object apply(Void input) {
                        setValue(instance).then(result);
                        return null;
                    }
                });
                return null;
            }
        });
        return result;
    }

    private Promise<Void> buildForm(final FormClass formClass) {
        this.relevanceHandler.formClassChanged();

        try {
            return widgetCreator.createWidgets(formClass, this).then(new Function<Void, Void>() {
                @Nullable
                @Override
                public Void apply(Void input) {
                    panelFiller.add(formClass, 0);
                    return null;
                }
            });

        } catch (Throwable caught) {
            return Promise.rejected(caught);
        }
    }

    public Promise<Void> setValue(FormInstance instance) {
        model.setWorkingRootInstance(instance);

        List<Promise<Void>> tasks = Lists.newArrayList();

        for (FieldContainer container : widgetCreator.getContainers().values()) {
            FormField field = container.getField();
            FieldValue value = model.getWorkingRootInstance().get(field.getId(), field.getType());

            if (value != null && value.getTypeClass() == field.getType().getTypeClass()) {
                tasks.add(container.getFieldWidget().setValue(value));
            } else {
                container.getFieldWidget().clearValue();
            }
            container.setValid();
        }

        return Promise.waitAll(tasks).then(new Function<Void, Void>() {
            @Override
            public Void apply(Void input) {
                relevanceHandler.onValueChange(); // invoke relevance handler once values are set
                return null;
            }
        });
    }

    /**
     * Returns selected key/tab for given field or otherwise null if nothing is selected or it is root instance (no key).
     *
     * @return selected key/tab for given field or otherwise null if nothing is selected or it is root instance (no key).
     */
    public String getSelectedKey(FormField field) {
        FormClass formClass = model.getClassByField(field.getId());
        if (formClass.isSubForm()) {
            SubFormPanel subformPanel = widgetCreator.getSubformPanel(formClass);
            if (subformPanel instanceof PeriodSubFormPanel) {
                PeriodSubFormPanel periodSubFormPanel = (PeriodSubFormPanel) subformPanel;
                Tab selectedTab = periodSubFormPanel.getSelectedTab();
                if (selectedTab != null) {
                    return selectedTab.getId();
                }
            }
        }
        return null;
    }

    public void onFieldUpdated(FormField field, FieldValue newValue) {
        Optional<FormInstance> workingInstance = model.getWorkingInstance(field.getId(), getSelectedKey(field));

        if (workingInstance.isPresent()) {
            if (!Objects.equals(workingInstance.get().get(field.getId()), newValue)) {
                workingInstance.get().set(field.getId(), newValue);
                relevanceHandler.onValueChange(); // skip handler must be applied after workingInstance is updated
            }
            validateField(widgetCreator.get(field.getId()));
            model.getChangedInstances().add(workingInstance.get());
        } else {
            FormClass formClass = model.getClassByField(field.getId());
            if (formClass.isSubForm()) {
                widgetCreator.get(field.getId()).setInvalid(I18N.CONSTANTS.subFormTabNotSelected());
                widgetCreator.get(field.getId()).getFieldWidget().clearValue();
            }
        }
    }

    private boolean validateField(FieldContainer container) {
        FormField field = container.getField();
        FieldValue value = getCurrentValue(field);
        if (value != null && value.getTypeClass() != field.getType().getTypeClass()) {
            value = null;
        }

        if (!container.getFieldWidget().isValid()) {
            return false;
        }

        Optional<Boolean> validatedBuiltInDates = validateBuiltinDates(container, field);
        if (validatedBuiltInDates.isPresent()) {
            return validatedBuiltInDates.get();
        }

        if (field.isRequired() && isEmpty(value) && field.isVisible() && !container.getFieldWidget().isReadOnly()) { // if field is not visible user doesn't have chance to fix it
            container.setInvalid(I18N.CONSTANTS.requiredFieldMessage());
            return false;
        } else {
            container.setValid();
            return true;
        }
    }

    private Optional<Boolean> validateBuiltinDates(FieldContainer container, FormField field) {
        if (BuiltinFields.isBuiltInDate(field.getId())) {
            FormClass rootFormClass = getModel().getRootFormClass();
            DateRange dateRange = BuiltinFields.getDateRange(getModel().getWorkingRootInstance(), rootFormClass);

            if (!rootFormClass.getLocks().isEmpty()) {
                if (new LockEvaluator(rootFormClass).isLockedSilently(getModel().getWorkingRootInstance())) {
                    getWidgetCreator().get(BuiltinFields.getStartDateField(rootFormClass).getId()).setInvalid(I18N.CONSTANTS.siteIsLocked());
                    getWidgetCreator().get(BuiltinFields.getEndDateField(rootFormClass).getId()).setInvalid(I18N.CONSTANTS.siteIsLocked());
                    return Optional.of(false);
                }
            }

            if (!dateRange.isValidWithNull()) {
                container.setInvalid(I18N.CONSTANTS.inconsistentDateRangeWarning());
                return Optional.of(false);
            } else {
                if (dateRange.isValid()) {
                    getWidgetCreator().get(BuiltinFields.getStartDateField(rootFormClass).getId()).setValid();
                    getWidgetCreator().get(BuiltinFields.getEndDateField(rootFormClass).getId()).setValid();
                    return Optional.of(true);
                }
            }
        }
        return Optional.absent();
    }

    private boolean isEmpty(FieldValue value) {
        return value == null ||
                (value instanceof EnumValue && ((EnumValue) value).getResourceIds().isEmpty()) ||
                (value instanceof ReferenceValue && ((ReferenceValue) value).getResourceIds().isEmpty()) ||
                (value instanceof AttachmentValue && ((AttachmentValue) value).getValues().isEmpty());
    }

    public boolean validate() {
        boolean valid = true;
        for (FieldContainer container : this.widgetCreator.getContainers().values()) {
            if (!validateField(container)) {
                valid = false;
            }
        }
        return valid;
    }

    private FieldValue getCurrentValue(FormField field) {
        Optional<FormInstance> instance = model.getWorkingInstance(field.getId(), getSelectedKey(field));
        return instance.isPresent() ? instance.get().get(field.getId()) : null;
    }

    @Override
    public Widget asWidget() {
        return withScroll ? scrollPanel : panel;
    }

    public FormWidgetCreator getWidgetCreator() {
        return widgetCreator;
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public FormActions getFormActions() {
        return formActions;
    }

    public RelevanceHandler getRelevanceHandler() {
        return relevanceHandler;
    }

    public void setHeadingVisible(boolean isHeadingVisible) {
        panelFiller.setHeadingVisible(isHeadingVisible);
    }
}
