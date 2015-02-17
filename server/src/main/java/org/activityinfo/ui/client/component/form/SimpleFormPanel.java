package org.activityinfo.ui.client.component.form;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.*;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.form.subform.SubFormTabsManipulator;
import org.activityinfo.ui.client.widget.DisplayWidget;

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
    private final ResourceLocator locator;
    private final RelevanceHandler relevanceHandler;
    private final FormWidgetCreator widgetCreator;

    public SimpleFormPanel(ResourceLocator locator, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory) {
        this(locator, containerFactory, widgetFactory, true);
    }

    public SimpleFormPanel(ResourceLocator locator, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory, boolean withScroll) {
        FormPanelStyles.INSTANCE.ensureInjected();

        Preconditions.checkNotNull(locator);
        Preconditions.checkNotNull(containerFactory);
        Preconditions.checkNotNull(widgetFactory);

        this.locator = locator;
        this.model = new FormModel(locator);
        this.withScroll = withScroll;
        this.relevanceHandler = new RelevanceHandler(this);
        this.widgetCreator = new FormWidgetCreator(model, containerFactory, widgetFactory);

        panel = new FlowPanel();
        panel.setStyleName(FormPanelStyles.INSTANCE.formPanel());
        scrollPanel = new ScrollPanel(panel);
    }

    public FormModel getModel() {
        return model;
    }

    public FormInstance getInstance() {
        return model.getWorkingInstance();
    }

    @Override
    public Promise<Void> show(final FormInstance instance) {
        return show(instance.asResource());
    }

    public Promise<Void> show(final Resource instance) {
        model.setInstance(instance);
        return model.loadFormClassWithDependentSubForms(instance.getResourceId("classId")).then(new Function<Void, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Void input) {
                return buildForm(model.getRootFormClass());
            }
        }).join(new Function<Promise<Void>, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Promise<Void> input) {
                return setValue(instance);
            }
        });
    }

    private Promise<Void> buildForm(final FormClass formClass) {
        this.relevanceHandler.formClassChanged();

        try {
            return widgetCreator.createWidgets(formClass, this).then(new Function<Void, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable Void input) {
                    addFormElements(formClass, 0);
                    return null;
                }
            });

        } catch (Throwable caught) {
            return Promise.rejected(caught);
        }
    }


    public Promise<Void> setValue(Resource instance) {
        model.setInstance(instance);
        model.setWorkingInstance(FormInstance.fromResource(instance));

        List<Promise<Void>> tasks = Lists.newArrayList();

        for (FieldContainer container : widgetCreator.getContainers().values()) {
            FormField field = container.getField();
            FieldValue value = model.getWorkingInstance().get(field.getId(), field.getType());

            if(value != null && value.getTypeClass() == field.getType().getTypeClass()) {
                tasks.add(container.getFieldWidget().setValue(value));
            } else {
                container.getFieldWidget().clearValue();
            }
            container.setValid();
        }

        return Promise.waitAll(tasks).then(new Function<Void, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable Void input) {
                relevanceHandler.onValueChange(); // invoke relevance handler once values are set
                return null;
            }
        });
    }

    private void addFormElements(FormElementContainer container, int depth) {
        for (FormElement element : container.getElements()) {
            if (element instanceof FormSection) {
                panel.add(createHeader(depth, element.getLabel()));
                addFormElements((FormElementContainer) element, depth + 1);
            } else if (element instanceof FormField) {
                FormField formField = (FormField) element;
                if (formField.isVisible()) {
                    if (formField.getType() instanceof SubFormType) {
                        FormClass subForm = getModel().getSubFormByFormFieldId(formField.getId());
                        final SubFormTabsManipulator subFormTabsManipulator = new SubFormTabsManipulator(locator);

                        panel.add(createHeader(depth, subForm.getLabel()));
                        panel.add(subFormTabsManipulator.getPresenter().getView());
                        subFormTabsManipulator.show(subForm);
                        addFormElements(subForm, depth + 1);
                    } else {
                        panel.add(widgetCreator.get(formField.getId()));
                    }
                }
            }
        }
    }

    public void onFieldUpdated(FormField field, FieldValue newValue) {
        if (!Objects.equals(model.getWorkingInstance().get(field.getId()), newValue)) {
            model.getWorkingInstance().set(field.getId(), newValue);
            relevanceHandler.onValueChange(); // skip handler must be applied after workingInstance is updated
        }
        validateField(widgetCreator.get(field.getId()));
    }

    private boolean validateField(FieldContainer container) {
        FormField field = container.getField();
        FieldValue value = getCurrentValue(field);
        if (value != null && value.getTypeClass() != field.getType().getTypeClass()) {
            value = null;
        }
        if (field.isRequired() && value == null && field.isVisible()) { // if field is not visible user doesn't have chance to fix it
            container.setInvalid(I18N.CONSTANTS.requiredFieldMessage());
            return false;
        } else {
            container.setValid();
            return true;
        }
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
        return model.getWorkingInstance().get(field.getId());
    }

    private static Widget createHeader(int depth, String header) {
        String hn = "h" + (3 + depth);
        return new HTML("<" + hn + ">" + SafeHtmlUtils.htmlEscape(header) + "</" + hn + ">");
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

}
