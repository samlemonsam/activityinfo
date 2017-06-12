package org.activityinfo.ui.client.component.formdesigner.properties;

import com.google.gwt.user.client.ui.IsWidget;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.ui.client.component.formdesigner.container.FieldWidgetContainer;

public abstract class TypeEditor<T extends FieldType> implements IsWidget {

    private FieldWidgetContainer currentField;

    @SuppressWarnings("unchecked")
    public final void show(FieldWidgetContainer container) {
        if(accept(container.getFormField().getType())) {
            this.currentField = container;
            show((T)container.getFormField().getType());
            asWidget().setVisible(true);

        } else {
            asWidget().setVisible(false);
        }
    }

    protected abstract boolean accept(FieldType type);

    @SuppressWarnings("unchecked")
    protected final T currentType() {
        return (T)currentField.getFormField().getType();
    }

    protected abstract void show(T type);

    protected final void updateType(T updatedType) {
        currentField.getFormField().setType(updatedType);
        currentField.syncWithModel();
    }
}
