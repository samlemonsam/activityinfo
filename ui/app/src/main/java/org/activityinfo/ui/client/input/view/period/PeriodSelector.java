package org.activityinfo.ui.client.input.view.period;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.sencha.gxt.widget.core.client.Component;
import com.sencha.gxt.widget.core.client.event.BeforeSelectEvent;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.input.viewModel.KeyedSubFormViewModel;

import java.util.List;

public interface PeriodSelector extends HasSelectionHandlers<ResourceId>, BeforeSelectEvent.HasBeforeSelectHandlers {

    List<Component> getToolBarItems();

    void update(KeyedSubFormViewModel viewModel);
}
