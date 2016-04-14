package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Pair;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

import java.util.List;
import java.util.Map;

/**
 * Created by yuriyz on 4/11/2016.
 */
public class ReferencePropertiesPresenter {

    private ReferenceProperties view;

    private HandlerRegistration referenceAddButtonClickHandler;
    private HandlerRegistration referenceRemoveButtonClickHandler;
    private Map<ResourceId, Pair<String, String>> formIdToLabelAndDatabase = Maps.newHashMap();

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

        locator.queryInstances(ClassCriteria.union(referenceType.getRange())).then(new AsyncCallback<List<FormInstance>>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(List<FormInstance> result) {
                for (FormInstance instance : result) {
                    int index = getIndexByValue(instance.getId());
                    view.getReferenceListBox().setItemText(index, ChooseFormTreeModel.labelFromInstance(instance));
                }
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
