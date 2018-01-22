package chdc.frontend.client.entry;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.InsertContainer;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormElement;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormSection;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.SubscriptionSet;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.view.InputHandler;
import org.activityinfo.ui.client.input.view.field.FieldWidget;
import org.activityinfo.ui.client.input.viewModel.FormInputViewModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Specialized Form Panel for incidents.
 *
 * The layout of the widgets is hardcoded for this form.
 */
public class IncidentFormPanel implements IsWidget {

    private final RecordRef recordRef;
    private final FormSource formSource;
    private final InputHandler inputHandler;
    private final SubscriptionSet subscriptionSet = new SubscriptionSet();
    private final FlowLayoutContainer container;

    private Map<ResourceId, FieldWidget> widgetMap;
    private Set<ResourceId> displayed = new HashSet<>();


    public IncidentFormPanel(FormSource formSource, RecordRef recordRef, InputHandler inputHandler, Observable<FormInputViewModel> viewModel) {
        this.formSource = formSource;
        this.recordRef = recordRef;
        this.inputHandler = inputHandler;

        container = new FlowLayoutContainer();
        container.addStyleName("data-entry-form");
        container.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if(event.isAttached()) {
                    subscriptionSet.add(viewModel.subscribe(vm -> onViewModelChanged(vm)));
                } else {
                    subscriptionSet.unsubscribeAll();
                }
            }
        });

    }

    @Override
    public Widget asWidget() {
        return container;
    }



    private void onViewModelChanged(Observable<FormInputViewModel> viewModel) {
        if(viewModel.isLoaded()) {
            if(widgetMap == null) {
                initWidgets(viewModel.get());
            } else {
                updateWidgets(viewModel.get());
            }
        }
    }


    private void initWidgets(FormInputViewModel viewModel) {
        widgetMap = new HashMap<>();

        IncidentFieldFactory factory = new IncidentFieldFactory(recordRef, inputHandler);

        // Create the widgets and add them to the panel
        FormClass formSchema = viewModel.getFormTree().getRootFormClass();
        for (FormElement rootElement : formSchema.getElements()) {
            if(rootElement instanceof FormSection) {

                FormSection section = (FormSection) rootElement;
                initFieldSet(factory, section);
            }
        }

        // Set initial values
        for (Map.Entry<ResourceId, FieldWidget> field : widgetMap.entrySet()) {
            ResourceId fieldId = field.getKey();
            FieldValue fieldValue = viewModel.getField(fieldId);
            if(fieldValue != null) {
                field.getValue().init(fieldValue);
            }
            if(viewModel.isRelevant(fieldId)) {
                displayed.add(fieldId);
            } else {
                field.getValue().asWidget().setVisible(false);
            }
        }
    }

    private void initFieldSet(IncidentFieldFactory factory, FormSection section) {
        FieldSetContainer fieldSet = new FieldSetContainer(section.getLabel());

        InsertContainer fieldContainer;
        if (shouldUseFormGrid(section)) {
            fieldContainer = new FlowLayoutContainer();
            fieldContainer.addStyleName("formgrid");
            fieldContainer.addStyleName("fieldset-section");
            fieldSet.add(fieldContainer);
        } else {
            // If this fieldset contains a single field, then do not
            // use the formgrid
            fieldContainer = fieldSet;
        }

        for (FormElement element : section.getElements()) {
            if(element instanceof FormField) {
                FormField field = (FormField) element;
                FieldWidget fieldWidget = factory.createWidget(field);
                if(fieldWidget != null) {
                    widgetMap.put(field.getId(), fieldWidget);
                    fieldContainer.add(fieldWidget);
                }
            }
        }

        if(fieldContainer.getWidgetCount() > 0) {
            container.add(fieldSet);
        }
    }

    private boolean shouldUseFormGrid(FormSection section) {
        switch (section.getId().asString()) {
            case "date_section":
            case "location_section":
            case "actor_section":
                return true;
            default:
                return false;
        }
    }


    private void updateWidgets(FormInputViewModel viewModel) {
        // Update the widgets to the current form status
        for (Map.Entry<ResourceId, FieldWidget> field : widgetMap.entrySet()) {
            boolean currentlyVisible = displayed.contains(field.getKey());
            boolean relevant = viewModel.isRelevant(field.getKey());
            if(currentlyVisible != relevant) {
                field.getValue().asWidget().setVisible(relevant);
                if(relevant) {
                    displayed.add(field.getKey());
                } else {
                    displayed.remove(field.getKey());
                }
            }
        }
    }

}
