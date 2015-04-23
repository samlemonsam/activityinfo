package org.activityinfo.ui.client.component.form.field;
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

import com.google.common.base.Strings;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import org.activityinfo.core.shared.type.formatter.DateFormatterFactory;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.event.FieldMessageEvent;
import org.activityinfo.ui.client.widget.DateBox;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * A text box that shows a {@link DatePicker} when the user focuses on it.
 * <p/>
 * <h3>CSS Style Rules</h3>
 * <p/>
 * <dl>
 * <dt>.gwt-DateBox</dt>
 * <dd>default style name</dd>
 * <dt>.dateBoxPopup</dt>
 * <dd>Applied to the popup around the DatePicker</dd>
 * <dt>.dateBoxFormatError</dt>
 * <dd>Default style for when the date box has bad input. Applied by
 * {@link DateBox.DefaultFormat} when the text does not represent a date that
 * can be parsed</dd>
 * </dl>
 * <p/>
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.DateBoxExample}
 * </p>
 */
public class DateFieldWidget implements FormFieldWidget<LocalDate> {

    public static final String FORMAT = DateFormatterFactory.FORMAT;

    @Nullable
    private final EventBus eventBus;
    private final ResourceId fieldId;
    private final DateBox dateBox;

    public DateFieldWidget(final ValueUpdater<LocalDate> valueUpdater,
                           @Nullable EventBus eventBus, ResourceId fieldId) {
        this.eventBus = eventBus;
        this.fieldId = fieldId;
        this.dateBox = new DateBox(new DatePicker(), null, createFormat());
        this.dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                valueUpdater.update(new LocalDate(event.getValue()));
            }
        });
        this.dateBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                validate();
            }
        });
    }

    public static DateBox.Format createFormat() {
        return new DateBox.DefaultFormat(DateTimeFormat.getFormat(FORMAT));
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.dateBox.setReadOnly(readOnly);
    }

    @Override
    public Promise<Void> setValue(LocalDate value) {
        dateBox.setValue(value.atMidnightInMyTimezone());
        return Promise.done();
    }

    @Override
    public void clearValue() {
        dateBox.setValue(null);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return dateBox;
    }

    private void validate() {
        if (eventBus == null) {
            return;
        }

        String valueAsString = dateBox.getTextBox().getValue();
        if (!Strings.isNullOrEmpty(valueAsString) &&
                this.dateBox.getFormat().parse(dateBox, valueAsString, false) == null) {
            eventBus.fireEvent(new FieldMessageEvent(fieldId, I18N.MESSAGES.dateFieldInvalidValue(FORMAT)));
        } else {
            eventBus.fireEvent(new FieldMessageEvent(fieldId, "").setClearMessage(true));
        }
    }
}

