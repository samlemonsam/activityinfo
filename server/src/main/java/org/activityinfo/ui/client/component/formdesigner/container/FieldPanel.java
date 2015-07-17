package org.activityinfo.ui.client.component.formdesigner.container;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import org.activityinfo.ui.client.component.form.subform.SubFormTabs;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;
import org.activityinfo.ui.client.component.formdesigner.event.HeaderSelectionEvent;
import org.activityinfo.ui.client.widget.ConfirmDialog;

/**
 * @author yuriyz on 7/8/14.
 */
public class FieldPanel {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);


    interface OurUiBinder extends UiBinder<Widget, FieldPanel> {
    }

    private final FormDesigner formDesigner;
    private ClickHandler clickHandler;

    @UiField
    Button removeButton;
    @UiField
    FocusPanel focusPanel;
    @UiField
    HTML label;
    @UiField
    SimplePanel widgetContainer;
    @UiField
    Label dragHandle;
    @UiField
    SubFormTabs subformTabs;

    public FieldPanel(FormDesigner formDesigner) {
        uiBinder.createAndBindUi(this);

        this.formDesigner = formDesigner;
        this.formDesigner.getEventBus().addHandler(HeaderSelectionEvent.TYPE, new HeaderSelectionEvent.Handler() {
            @Override
            public void handle(HeaderSelectionEvent event) {
                setSelected(false);
            }
        });

        this.focusPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                FieldPanel.this.onClick();
            }
        });
    }

    /**
     * Actual removing must be done only via remove confirmation callback.
     *
     * @return remove button
     * @see this.setOnRemoveConfirmationCallback
     */
    public Button getRemoveButton() {
        return removeButton;
    }

    public void setOnRemoveConfirmationCallback(final ClickHandler onRemoveConfirmationCallback) {
        removeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                ConfirmDialog.confirm(new DeleteWidgetContainerAction(getFocusPanel(), formDesigner) {
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        onRemoveConfirmationCallback.onClick(event);
                    }
                });

            }
        });
    }

    private void onClick() {
        setSelected(true);
        if (clickHandler != null) {
            clickHandler.onClick(null);
        }
    }

    public HTML getLabel() {
        return label;
    }

    public Widget asWidget() {
        return focusPanel;
    }

    public Label getDragHandle() {
        return dragHandle;
    }

    public SimplePanel getWidgetContainer() {
        return widgetContainer;
    }

    public SubFormTabs getSubformTabs() {
        return subformTabs;
    }

    public String getSelectedClassName() {
        return FormDesignerStyles.INSTANCE.widgetContainerSelected();
    }

    public void setSelected(boolean selected) {
        if (selected) {
            focusPanel.addStyleName(getSelectedClassName());
        } else {
            focusPanel.removeStyleName(getSelectedClassName());
        }
    }

    public FocusPanel getFocusPanel() {
        return focusPanel;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }
}


