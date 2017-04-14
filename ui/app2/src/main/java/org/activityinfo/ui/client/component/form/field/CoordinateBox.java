package org.activityinfo.ui.client.component.form.field;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;
import org.activityinfo.ui.client.widget.TextBox;

/**
 *
 */
class CoordinateBox implements IsWidget, HasValueChangeHandlers<Double> {


    interface CoordinateBoxUiBinder extends UiBinder<HTMLPanel, CoordinateBox> {
    }

    private static CoordinateBoxUiBinder ourUiBinder = GWT.create(CoordinateBoxUiBinder.class);


    private HTMLPanel panel;

    @UiField
    LabelElement controlLabel;

    @UiField
    TextBox textBox;
    @UiField
    SpanElement helpBlock;

    private SimpleEventBus eventBus = new SimpleEventBus();

    private final CoordinateParser parser;
    private Double value;

    private boolean valid;

    private String validationMessage;

    /**
     * True if the user has entered their own text in the input
     */
    private boolean dirtyText;

    public CoordinateBox(CoordinateAxis axis) {
        this.panel = ourUiBinder.createAndBindUi(this);
        this.controlLabel.setInnerText(axis.getLocalizedName());

        this.parser = new CoordinateParser(axis, JsCoordinateNumberFormatter.INSTANCE);
        textBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onTextChange();
            }
        });

        textBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if(dirtyText) {
                    if(valid && value != null) {
                        textBox.setValue(parser.format(value));
                        dirtyText = false;
                    }
                }
            }
        });

        this.valid = true;
        updateView();
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void setValue(Double value) {
        this.value = value;
        this.valid = true;
        this.dirtyText = false;
        if(value == null) {
            textBox.setValue("");
        } else {
            textBox.setValue(parser.format(value));
        }
        updateView();

    }

    private void onTextChange() {

        dirtyText = true;

        try {
            this.value = parser.parse(textBox.getValue());
            this.valid = true;
            this.validationMessage = null;

        } catch (CoordinateFormatException e) {
            this.value = null;
            this.valid = false;
            this.validationMessage = e.getMessage();
        }

        updateView();
        ValueChangeEvent.fire(this, this.value);
    }

    private void updateView() {
        if(valid) {
            panel.removeStyleName("has-error");
            helpBlock.getStyle().setDisplay(Style.Display.NONE);
        } else {
            panel.addStyleName("has-error");
            helpBlock.getStyle().setDisplay(Style.Display.BLOCK);
            helpBlock.setInnerText(validationMessage);
        }
    }


    public void setReadOnly(boolean readOnly) {
        textBox.setReadOnly(readOnly);
    }

    public boolean isValid() {
        return valid;
    }

    public Double getValue() {
        return value;
    }


    public boolean isReadOnly() {
        return textBox.isReadOnly();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Double> handler) {
        return eventBus.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }
}
