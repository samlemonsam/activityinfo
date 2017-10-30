package org.activityinfo.ui.client.input.view.period;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.sencha.gxt.widget.core.client.Component;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public interface PeriodSelector extends HasSelectionHandlers<ResourceId> {

    List<Component> getToolBarItems();



}
