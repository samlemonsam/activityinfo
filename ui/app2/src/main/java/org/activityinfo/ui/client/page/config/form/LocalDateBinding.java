package org.activityinfo.ui.client.page.config.form;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.binding.Converter;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.widget.form.DateField;

import java.util.Date;


public class LocalDateBinding {

    public static FieldBinding create(DateField dateField, String datePropertyName) {
        FieldBinding binding = new FieldBinding(dateField, datePropertyName);
        binding.setConverter(new Converter() {
            @Override
            public Object convertModelValue(Object value) {
                if(value == null) {
                    return null;
                } else {
                    return ((LocalDate) value).atMidnightInMyTimezone();
                }
            }

            @Override
            public Object convertFieldValue(Object value) {
                if(value == null) {
                    return null;
                } else {
                    Date dateValue = (Date) value;
                    return new LocalDate(dateValue);
                }
            }
        });
        return binding;
    }

}
