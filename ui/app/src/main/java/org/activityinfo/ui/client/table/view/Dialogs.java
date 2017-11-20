package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.Dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Dialogs {

    /**
     * Adds a one-time callback for this dialog that is called when the OK button is clicked.
     */
    public static <T> void addCallback(Dialog dialog, Consumer<T> callback, Supplier<T> supplier) {
        List<HandlerRegistration> registrations = new ArrayList<>();

        registrations.add(dialog.getButton(Dialog.PredefinedButton.OK).addSelectHandler(event -> {
            dialog.hide();
            callback.accept(supplier.get());
        }));
        registrations.add(dialog.addDialogHideHandler(event -> {
            for (HandlerRegistration registration : registrations) {
                registration.removeHandler();
            }
            registrations.clear();
        }));
    }

}
