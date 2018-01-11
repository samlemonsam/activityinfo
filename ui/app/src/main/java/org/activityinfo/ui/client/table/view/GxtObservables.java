package org.activityinfo.ui.client.table.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.widget.core.client.form.Field;
import org.activityinfo.observable.Observable;

/**
 * Adapter methods between GXT views and observables
 */
public class GxtObservables {

    public static <T> Observable<T> of(Field<T> field) {
        return new Observable<T>() {

            private HandlerRegistration handlerRegistration;

            @Override
            public boolean isLoading() {
                return field.getValue() == null;
            }

            @Override
            public T get() {
                return field.getValue();
            }

            @Override
            protected void onConnect() {
                super.onConnect();
                handlerRegistration = field.addValueChangeHandler(event -> fireChange());
            }

            @Override
            protected void onDisconnect() {
                super.onDisconnect();
                handlerRegistration.removeHandler();
                handlerRegistration = null;
            }
        };
    }
}
