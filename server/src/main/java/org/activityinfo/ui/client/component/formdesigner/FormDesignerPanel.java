package org.activityinfo.ui.client.component.formdesigner;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.model.type.subform.SubformConstants;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.FieldsHolderWidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.container.WidgetContainer;
import org.activityinfo.ui.client.component.formdesigner.drop.NullValueUpdater;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;
import org.activityinfo.ui.client.component.formdesigner.header.HeaderPanel;
import org.activityinfo.ui.client.component.formdesigner.palette.FieldPalette;
import org.activityinfo.ui.client.component.formdesigner.properties.ContainerPropertiesPanel;
import org.activityinfo.ui.client.component.formdesigner.properties.PropertiesPanel;
import org.activityinfo.ui.client.page.HasNavigationCallback;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.util.GwtUtil;
import org.activityinfo.ui.client.widget.Button;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main Form designer panel. Must be created via FormDesigner class
 *
 * @author yuriyz on 07/04/2014.
 * @see org.activityinfo.ui.client.component.formdesigner.FormDesigner
 */
public class FormDesignerPanel extends Composite implements ScrollHandler, HasNavigationCallback, FormSavedGuard.HasSavedGuard {

    private final static OurUiBinder uiBinder = GWT
            .create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, FormDesignerPanel> {
    }

    private final Map<ResourceId, WidgetContainer> containerMap = Maps.newHashMap();
    private ScrollPanel scrollAncestor;
    private WidgetContainer selectedWidgetContainer;
    private HasNavigationCallback savedGuard = null;

    @UiField
    HTMLPanel containerPanel;
    @UiField
    FlowPanel dropPanel;
    @UiField
    PropertiesPanel propertiesPanel;
    @UiField
    HeaderPanel headerPanel;
    @UiField
    FieldPalette fieldPalette;
    @UiField
    Button saveButton;
    @UiField
    HTML statusMessage;
    @UiField
    HTML spacer;
    @UiField
    HTML paletteSpacer;
    @UiField
    HTMLPanel containerPropertiesTab;
    @UiField
    ContainerPropertiesPanel containerPropertiesPanel;
    @UiField
    Anchor containerPropertiesTabLink;
    @UiField
    Anchor propertiesTabLink;
    @UiField
    HTMLPanel propertiesTab;
    @UiField
    LIElement containerPropertiesTabLi;
    @UiField
    LIElement propertiesTabLi;

    /**
     * Main FormDesigner panel. It must be created via FormDesigner only.
     *
     * @param formClass    form class
     * @param formDesigner form designer
     */
    protected FormDesignerPanel(@Nonnull final FormClass formClass, final FormDesigner formDesigner) {
        FormDesignerStyles.INSTANCE.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        propertiesPanel.setVisible(false);

        addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                scrollAncestor = GwtUtil.getScrollAncestor(FormDesignerPanel.this);
                scrollAncestor.addScrollHandler(FormDesignerPanel.this);
            }
        });
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                savedGuard = formDesigner.getSavedGuard();
                List<Promise<Void>> promises = Lists.newArrayList();
                buildWidgetContainers(formDesigner, formClass, formClass, 0, promises);
                Promise.waitAll(promises).then(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // ugly but we still have exceptions like: unsupportedoperationexception: domain is not supported.
                        fillPanel(formClass, formDesigner);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        fillPanel(formClass, formDesigner);
                    }
                });

            }
        });
    }

    public void bind(EventBus eventBus) {
        eventBus.addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                selectedWidgetContainer = event.getSelectedItem();
                calcSpacerHeight();
            }
        });

        propertiesTabLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setPropertiesTabSelected();
            }
        });
        containerPropertiesTabLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setContainerPropertiesTabSelected();
            }
        });
    }

    public void setContainerPropertiesTabSelected() {
        containerPropertiesTabLi.addClassName("active");
        propertiesTabLi.removeClassName("active");

        containerPropertiesTab.setVisible(true);
        propertiesTab.setVisible(false);
    }

    public void setPropertiesTabSelected() {
        propertiesTabLi.addClassName("active");
        containerPropertiesTabLi.removeClassName("active");

        propertiesTab.setVisible(true);
        containerPropertiesTab.setVisible(false);
    }

    private void fillPanel(final FormClass formClass, final FormDesigner formDesigner) {

        // Exclude legacy builtin fields that the user won't be able to remove or reorder
        final Set<ResourceId> builtinFields = builtinFields(formClass.getId());

        formClass.traverse(formClass, new TraverseFunction() {
            @Override
            public void apply(FormElement element, FormElementContainer container) {
                if (element instanceof FormField) {
                    if (!builtinFields.contains(element.getId())) {
                        FormField formField = (FormField) element;
                        WidgetContainer widgetContainer = containerMap.get(formField.getId());
                        if (widgetContainer != null) { // widget container may be null if domain is not supported, should be removed later
                            Widget widget = widgetContainer.asWidget();
                            formDesigner.getDragController().makeDraggable(widget, widgetContainer.getDragHandle());

                            FlowPanel parentDropPanel = (FlowPanel) formDesigner.getDropControllerRegistry().getDropController(widgetContainer.getParentId()).getDropTarget();
                            parentDropPanel.add(widget);
                        }
                        if (formField.getType() instanceof SubFormType) {
                            ResourceId subFormId = ((SubFormType) formField.getType()).getClassId();
                            FormClass subForm = (FormClass) formDesigner.getModel().getElementContainer(subFormId);
                            fillPanel(subForm, formDesigner);
                        }
                    }
                } else if (element instanceof FormSection) {
                    FormSection section = (FormSection) element;
                    WidgetContainer widgetContainer = containerMap.get(section.getId());
                    Widget widget = widgetContainer.asWidget();
                    formDesigner.getDragController().makeDraggable(widget, widgetContainer.getDragHandle());
                    dropPanel.add(widget);

                } else {
                    throw new UnsupportedOperationException("Unknown form element.");
                }
            }
        });
    }

    private Set<ResourceId> builtinFields(ResourceId formClassId) {
        Set<ResourceId> fieldIds = new HashSet<>();
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.START_DATE_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.END_DATE_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.COMMENT_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.PARTNER_FIELD));
        fieldIds.add(CuidAdapter.field(formClassId, CuidAdapter.PROJECT_FIELD));
        fieldIds.add(SubformConstants.TYPE_FIELD_ID);
        fieldIds.add(SubformConstants.TAB_COUNT_FIELD_ID);
        return fieldIds;
    }

    private void buildWidgetContainers(final FormDesigner formDesigner, final FormElementContainer container, final FormClass owner, final int depth, final List<Promise<Void>> promises) {
        for (FormElement element : container.getElements()) {
            if (element instanceof FormSection) {
                FormSection formSection = (FormSection) element;
                containerMap.put(formSection.getId(), FieldsHolderWidgetContainer.section(formDesigner, formSection, container.getId()));
                buildWidgetContainers(formDesigner, formSection, owner, depth + 1, promises);
            } else if (element instanceof FormField) {
                final FormField formField = (FormField) element;
                if (formField.getType() instanceof SubFormType) { // subform
                    SubFormType subform = (SubFormType) formField.getType();

                    Promise<Void> promise = formDesigner.getResourceLocator().getFormClass(subform.getClassId()).then(new Function<FormClass, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nullable FormClass subform) {
                            formDesigner.getModel().registerSubform(formField.getId(), subform);
                            containerMap.put(formField.getId(), FieldsHolderWidgetContainer.subform(formDesigner, subform, container.getId()));
                            buildWidgetContainers(formDesigner, subform, subform, depth + 1, promises);
                            return null;
                        }
                    });
                    promises.add(promise);

                } else { // regular formfield
                    Promise<Void> promise = formDesigner.getFormFieldWidgetFactory().createWidget(owner, formField, NullValueUpdater.INSTANCE).then(new Function<FormFieldWidget, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nullable FormFieldWidget input) {
                            containerMap.put(formField.getId(), new FieldWidgetContainer(formDesigner, input, formField, container.getId()));
                            return null;
                        }
                    });
                    promises.add(promise);
                }
            }
        }
    }

    @Override
    public void onScroll(ScrollEvent event) {
        calcSpacerHeight();
    }

    private void calcSpacerHeight() {
        int verticalScrollPosition = scrollAncestor.getVerticalScrollPosition();
        if (verticalScrollPosition > FormDesignerConstants.MAX_VERTICAL_SCROLL_POSITION) {
            int height = verticalScrollPosition - FormDesignerConstants.MAX_VERTICAL_SCROLL_POSITION;

//            int selectedWidgetTop = 0;
//            if (selectedWidgetContainer != null) {
//                selectedWidgetTop = selectedWidgetContainer.asWidget().getAbsoluteTop();
//            }
//            if (selectedWidgetTop < 0) {
//                height = height + selectedWidgetTop;
//            }

            //GWT.log("verticalPos = " + verticalScrollPosition + ", height = " + height + ", selectedWidgetTop = " + selectedWidgetTop);
            spacer.setHeight(height + "px");
            paletteSpacer.setHeight(height + "px");
        } else {
            spacer.setHeight("0px");
            paletteSpacer.setHeight("0px");
        }
    }

    public Map<ResourceId, WidgetContainer> getContainerMap() {
        return containerMap;
    }

    public FlowPanel getDropPanel() {
        return dropPanel;
    }

    public PropertiesPanel getPropertiesPanel() {
        return propertiesPanel;
    }

    public HeaderPanel getHeaderPanel() {
        return headerPanel;
    }

    public ContainerPropertiesPanel getContainerPropertiesPanel() {
        return containerPropertiesPanel;
    }

    public FieldPalette getFieldPalette() {
        return fieldPalette;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public HTML getStatusMessage() {
        return statusMessage;
    }

    public HasNavigationCallback getSavedGuard() {
        return savedGuard;
    }

    public void setSavedGuard(HasNavigationCallback savedGuard) {
        this.savedGuard = savedGuard;
    }

    @Override
    public void navigate(NavigationCallback callback) {
        if (savedGuard != null) {
            savedGuard.navigate(callback);
        }
    }

}
