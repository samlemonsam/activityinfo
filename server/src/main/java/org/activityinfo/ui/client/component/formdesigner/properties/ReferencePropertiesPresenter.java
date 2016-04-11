package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Pair;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

import java.util.Map;

/**
 * Created by yuriyz on 4/11/2016.
 */
public class ReferencePropertiesPresenter {

    private ResourceLocator locator;
    private ReferenceProperties view;

    private HandlerRegistration referenceAddButtonClickHandler;
    private HandlerRegistration referenceRemoveButtonClickHandler;
    private Map<ResourceId, Pair<String, String>> formIdToLabelAndDatabase = Maps.newHashMap();

    public ReferencePropertiesPresenter(ResourceLocator locator, ReferenceProperties view) {
        this.locator = locator;
        this.view = view;
    }

    public void reset() {
        view.setVisible(false);

        if (referenceAddButtonClickHandler != null) {
            referenceAddButtonClickHandler.removeHandler();
        }
        if (referenceRemoveButtonClickHandler != null) {
            referenceRemoveButtonClickHandler.removeHandler();
        }
    }

    public void show(final FieldWidgetContainer fieldWidgetContainer) {
        final FormField formField = fieldWidgetContainer.getFormField();

        referenceAddButtonClickHandler = view.getReferenceAddButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ChooseFormDialog dialog = new ChooseFormDialog(fieldWidgetContainer.getFormDesigner().getResourceLocator());
                dialog.show().setOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        // todo put in cache
                        ReferenceType referenceType = (ReferenceType) formField.getType();
                        referenceType.getRange().addAll(dialog.getFormClassIds());
                        setReferenceListItems(referenceType);
                    }
                });
            }
        });
        referenceRemoveButtonClickHandler = view.getReferenceRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ReferenceType referenceType = (ReferenceType) formField.getType();

                for (int i = 0; i < view.getReferenceListBox().getItemCount(); i++) {
                    if (view.getReferenceListBox().isItemSelected(i)) {
                        ResourceId resourceId = ResourceId.valueOf(view.getReferenceListBox().getValue(i));
                        referenceType.getRange().remove(resourceId);
                    }
                }
                setReferenceListItems(referenceType);
            }
        });

    }


    private void setReferenceListItems(ReferenceType referenceType) {
        for (ResourceId resourceId : referenceType.getRange()) {
            view.getReferenceListBox().addItem(resourceId.asString(), resourceId.asString());
        }
    }
}
