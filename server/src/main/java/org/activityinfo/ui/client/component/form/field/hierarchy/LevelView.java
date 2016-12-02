package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Supplier;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import org.activityinfo.promise.Promise;

import java.util.List;


public interface LevelView extends HasSelectionHandlers<Choice> {


    void clearSelection();

    void setReadOnly(boolean readOnly);

    void setEnabled(boolean enabled);

    void setSelection(Choice selection);

    void setChoices(Supplier<Promise<List<Choice>>> choices);
}
