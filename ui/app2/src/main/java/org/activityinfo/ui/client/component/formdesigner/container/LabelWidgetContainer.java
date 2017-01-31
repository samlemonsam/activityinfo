package org.activityinfo.ui.client.component.formdesigner.container;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormLabel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;
import org.activityinfo.ui.client.widget.ConfirmDialog;

/**
 * Created by yuriyz on 4/15/2016.
 */
public class LabelWidgetContainer implements WidgetContainer {

    private FormDesigner formDesigner;
    private FormLabel formLabel;
    private final FieldPanel fieldPanel;
    private final ResourceId parentId;

    public LabelWidgetContainer(final FormDesigner formDesigner, final FormLabel formLabel, final ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.formLabel = formLabel;
        this.parentId = parentId;

        fieldPanel = new FieldPanel(formDesigner);
        fieldPanel.getRemoveButton().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                ConfirmDialog.confirm(new DeleteWidgetContainerAction(fieldPanel.getFocusPanel(), formDesigner) {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        FormClass formClass = (FormClass) formDesigner.getModel().getElementContainer(parentId); // get root or subform formclass
                        formClass.remove(formLabel);
                    }
                });
            }
        });

        fieldPanel.setClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getEventBus().fireEvent(new WidgetContainerSelectionEvent(LabelWidgetContainer.this));
            }
        });
        this.formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer selectedItem = event.getSelectedItem();
                fieldPanel.setSelected(selectedItem.asWidget().equals(fieldPanel.asWidget()));
            }
        });

        fieldPanel.asWidget().getElement().setAttribute(FormDesignerConstants.DATA_FIELD_ID, formLabel.getId().asString());
        syncWithModel();
    }

    @Override
    public ResourceId getParentId() {
        return parentId;
    }

    public void syncWithModel() {
        final SafeHtmlBuilder label = new SafeHtmlBuilder();

        label.append(SafeHtmlUtils.fromString(Strings.nullToEmpty(formLabel.getLabel())));

        String labelHtml = label.toSafeHtml().asString();
        if (!formLabel.isVisible()) {
            labelHtml = "<del>" + labelHtml + "</del>";
        }

        fieldPanel.getLabel().setHTML(labelHtml);
    }

    @Override
    public void syncWithModel(boolean force) {
        syncWithModel();
    }

    @Override
    public Widget asWidget() {
        return fieldPanel.asWidget();
    }

    @Override
    public Widget getDragHandle() {
        return fieldPanel.getDragHandle();
    }

    public FormLabel getFormLabel() {
        return formLabel;
    }

    public FormDesigner getFormDesigner() {
        return formDesigner;
    }
}
