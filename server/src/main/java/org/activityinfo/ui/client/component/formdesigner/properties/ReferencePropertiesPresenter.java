package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
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

    private HandlerRegistration addButtonClickHandler;
    private HandlerRegistration removeButtonClickHandler;
    private HandlerRegistration changeHandler;

    public ReferencePropertiesPresenter(ReferenceProperties view) {
        this.view = view;
    }

    public void reset() {
        view.setVisible(false);

        if (addButtonClickHandler != null) {
            addButtonClickHandler.removeHandler();
        }
        if (removeButtonClickHandler != null) {
            removeButtonClickHandler.removeHandler();
        }
        if (changeHandler != null) {
            changeHandler.removeHandler();
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
        setListItems(referenceType, locator);

        addButtonClickHandler = view.getAddButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ChooseFormDialog dialog = new ChooseFormDialog(fieldWidgetContainer.getFormDesigner().getResourceLocator());
                dialog.show().setOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        referenceType.getRange().addAll(dialog.getFormClassIds());
                        setListItems(referenceType, locator);
                    }
                });
            }
        });
        removeButtonClickHandler = view.getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (int i = 0; i < view.getListBox().getItemCount(); i++) {
                    if (view.getListBox().isItemSelected(i)) {
                        ResourceId resourceId = ResourceId.valueOf(view.getListBox().getValue(i));
                        referenceType.getRange().remove(resourceId);
                    }
                }
                setListItems(referenceType, locator);
            }
        });
        changeHandler = view.getListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                view.getRemoveButton().setEnabled(view.getListBox().getSelectedIndex() != -1);
            }
        });
    }


    private void setListItems(final ReferenceType referenceType, final ResourceLocator locator) {
        view.getListBox().clear();

        for (ResourceId resourceId : referenceType.getRange()) {
            view.getListBox().addItem(resourceId.asString(), resourceId.asString());
        }

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
                    view.getListBox().setItemText(index, result.getLabel());
                    putParentLabel(result, result, locator);
                }
            });
        }
    }

    private void putParentLabel(final FormClass leaf, FormClass child, final ResourceLocator locator) {
        if (child.getParentFormId().isPresent()) {
            locator.getFormClass(child.getParentFormId().get()).then(new AsyncCallback<FormClass>() {
                @Override
                public void onFailure(Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                }

                @Override
                public void onSuccess(FormClass owner) {
                    int index = getIndexByValue(leaf.getId());

                    String label = view.getListBox().getItemText(index);
                    label = owner.getLabel() + " > " + label;
                    view.getListBox().setItemText(index, label);

                    SelectElement selectElement = view.getListBox().getElement().cast(); // set tooltip
                    selectElement.getOptions().getItem(index).setTitle(label);

                    putParentLabel(leaf, owner, locator);
                }
            });
        }
    }

    private int getIndexByValue(ResourceId resourceId) {
        for (int i = 0; i < view.getListBox().getItemCount(); i++) {
            if (view.getListBox().getValue(i).equals(resourceId.asString())) {
                return i;
            }
        }
        throw new RuntimeException("Unknown resourceId:" + resourceId);
    }
}
