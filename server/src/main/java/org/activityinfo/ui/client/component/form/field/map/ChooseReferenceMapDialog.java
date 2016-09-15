package org.activityinfo.ui.client.component.form.field.map;

import com.google.common.base.Preconditions;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.ui.client.style.ModalStylesheet;
import org.activityinfo.ui.client.widget.ModalDialog;

import java.util.Set;

/**
 * Created by yuriyz on 9/13/2016.
 */
public class ChooseReferenceMapDialog implements HasSelectionHandlers<ReferenceValue> {

    private final ModalDialog dialog;
    private final SimpleEventBus eventBus = new SimpleEventBus();

    public ChooseReferenceMapDialog(Set<MapItem> items) {

        ModalStylesheet.INSTANCE.ensureInjected();

        dialog = new ModalDialog(null);

        dialog.setDialogTitle(I18N.CONSTANTS.chooseReferenceValue());
        dialog.getDialogDiv().getStyle().setWidth(width(), Style.Unit.PX);

        dialog.getPrimaryButton().setText(I18N.CONSTANTS.select());
        dialog.getPrimaryButton().setStyleName("btn btn-primary");
        dialog.getPrimaryButton().setEnabled(false);
    }

    public void show(SelectionHandler<ReferenceValue> callback) {
        Preconditions.checkNotNull(callback);
        addSelectionHandler(callback);

        dialog.show();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<ReferenceValue> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    public static int width() {
        return Window.getClientWidth() - 100;
    }

    public static int height() {
        return Window.getClientHeight() - 100;
    }
}
