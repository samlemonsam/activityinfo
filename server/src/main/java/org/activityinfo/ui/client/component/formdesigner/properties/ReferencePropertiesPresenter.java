package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

/**
 * Created by yuriyz on 4/11/2016.
 */
public class ReferencePropertiesPresenter {

    private ReferenceProperties view;

    private HandlerRegistration referenceAddButtonClickHandler;
    private HandlerRegistration referenceRemoveButtonClickHandler;

    public ReferencePropertiesPresenter(ReferenceProperties view) {
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
        final boolean isVisible = formField.getType() instanceof ReferenceType;

        view.setVisible(isVisible);
        if (!isVisible) {
            return;
        }

        final ResourceLocator locator = fieldWidgetContainer.getFormDesigner().getResourceLocator();
        final ReferenceType referenceType = (ReferenceType) formField.getType();

        referenceAddButtonClickHandler = view.getReferenceAddButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ChooseFormDialog dialog = new ChooseFormDialog(fieldWidgetContainer.getFormDesigner().getResourceLocator());
                dialog.show().setOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        referenceType.getRange().addAll(dialog.getFormClassIds());
                        setReferenceListItems(referenceType, locator);
                    }
                });
            }
        });
        referenceRemoveButtonClickHandler = view.getReferenceRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < view.getReferenceListBox().getItemCount(); i++) {
                    if (view.getReferenceListBox().isItemSelected(i)) {
                        ResourceId resourceId = ResourceId.valueOf(view.getReferenceListBox().getValue(i));
                        referenceType.getRange().remove(resourceId);
                    }
                }
                setReferenceListItems(referenceType, locator);
            }
        });

    }


    private void setReferenceListItems(final ReferenceType referenceType, final ResourceLocator locator) {
        view.getReferenceListBox().clear();

        for (ResourceId resourceId : referenceType.getRange()) {
            view.getReferenceListBox().addItem(resourceId.asString(), resourceId.asString());
        }
        view.getReferenceRemoveButton().setEnabled(!referenceType.getRange().isEmpty());

        updateLabels(referenceType, locator);
    }

    private void updateLabels(ReferenceType referenceType, final ResourceLocator locator) {
        for (ResourceId resourceId : referenceType.getRange()) {
            locator.getFormClass(resourceId).then(new AsyncCallback<FormClass>() {
                @Override
                public void onFailure(Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(FormClass result) {
                    int index = getIndexByValue(result.getId());
                    view.getReferenceListBox().setItemText(index, result.getLabel());
                    putParentLabel(result, result, locator);
                }
            });
        }
    }

    private void putParentLabel(final FormClass leaf, FormClass child, final ResourceLocator locator) {
        if (child.getOwnerId() == null) {
            return;
        }

        locator.getFormClass(child.getOwnerId()).then(new AsyncCallback<FormClass>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(FormClass owner) {
                int index = getIndexByValue(leaf.getId());

                String label = view.getReferenceListBox().getItemText(index);
                label = owner.getLabel() + " > " + label;
                view.getReferenceListBox().setItemText(index, label);

                putParentLabel(leaf, owner, locator);
            }
        });
    }

    private int getIndexByValue(ResourceId resourceId) {
        for (int i = 0; i < view.getReferenceListBox().getItemCount(); i++) {
            if (view.getReferenceListBox().getValue(i).equals(resourceId.asString())) {
                return i;
            }
        }
        throw new RuntimeException("Unknown resourceId:" + resourceId);
    }
}
