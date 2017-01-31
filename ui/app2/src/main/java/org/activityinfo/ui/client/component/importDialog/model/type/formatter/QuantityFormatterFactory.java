package org.activityinfo.ui.client.component.importDialog.model.type.formatter;

/**
 * Creates a {@code QuantityFormatter} for a given FormField.
 *
 * <p>Provides a layer of abstraction over {@link com.google.gwt.i18n.client.NumberFormat} and
 * {@link java.text.NumberFormat}</p>
 */
public interface QuantityFormatterFactory {

    QuantityFormatter create();

}

