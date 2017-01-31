package org.activityinfo.ui.client.component.filter;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetActivityForms;
import org.activityinfo.legacy.shared.command.result.ActivityFormResults;
import org.activityinfo.legacy.shared.command.result.AttributeGroupResult;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.AttributeGroupDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;

import java.util.*;

import static org.activityinfo.ui.client.component.filter.AttributeGroupFilterWidget.DIMENSION_TYPE;

public class AttributeGroupFilterWidgets implements FilterPanel {
    private final Dispatcher service;
    private final ContentPanel panel;
    private ValueChangeHandler<Filter> valueChangeHandler;
    private SuccessCallback<Void> drawCallback;

    private Filter prevFilter = new Filter();
    ;
    private AttributeGroupResult prevResult;

    private List<AttributeGroupFilterWidget> widgets;

    public AttributeGroupFilterWidgets(ContentPanel panel,
                                       Dispatcher service,
                                       ValueChangeHandler<Filter> valueChangeHandler,
                                       SuccessCallback<Void> drawCallback) {

        this.service = service;
        this.panel = panel;

        this.widgets = Lists.newArrayList();

        this.valueChangeHandler = valueChangeHandler;
        this.drawCallback = drawCallback;
    }

    public void draw(final Filter filter) {
        final Filter value = new Filter(filter);
        value.clearRestrictions(DIMENSION_TYPE);

        // prevents executing the same command needlessly
        if (prevFilter == null || !prevFilter.equals(filter)) {
            prevFilter = filter;

            Log.debug("AttributeGroupFilterWidgets called for filter " + filter);

            service.execute(new GetActivityForms(filter), new AsyncCallback<ActivityFormResults>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failed to load schema", caught);
                }

                @Override
                public void onSuccess(final ActivityFormResults forms) {
                    // clean up old widgets
                    for (AttributeGroupFilterWidget widget : widgets) {
                        panel.remove(widget);
                    }

                    // create new widgets, one for each attributegroup.
                    // remember the old selection
                    Set<Integer> selection = getSelectedIds();

                    widgets = new ArrayList<AttributeGroupFilterWidget>();
                    Set<String> attributesByName = Sets.newHashSet();
                    for (ActivityFormDTO form : forms.getData()) {
                        for (AttributeGroupDTO group : form.getAttributeGroups()) {
                            
                            String key = group.getName().toLowerCase();
                            if(!attributesByName.contains(key)) {

                                AttributeGroupFilterWidget widget = new AttributeGroupFilterWidget(group);
                                widget.setSelection(selection);
                                if(valueChangeHandler != null) {
                                    widget.addValueChangeHandler(valueChangeHandler);
                                }

                                panel.add(widget);
                                widgets.add(widget);
                                attributesByName.add(key);
                            }
                        }
                    }

                    if (drawCallback != null) {
                        drawCallback.onSuccess(null);
                    }
                }
            });
        }
    }

    private Set<Integer> getSelectedIds() {
        Set<Integer> set = new HashSet<>();
        for (AttributeGroupFilterWidget widget : widgets) {
            set.addAll(widget.getValue().getRestrictions(DIMENSION_TYPE));
        }
        return set;
    }

    public void clearFilter() {
        for (AttributeGroupFilterWidget widget : widgets) {
            widget.clear();
        }
    }

    @Override
    public Filter getValue() {
        Filter filter = new Filter();
        Set<Integer> selectedIds = getSelectedIds();
        if (selectedIds.size() > 0) {
            filter.addRestriction(DIMENSION_TYPE, selectedIds);
        }
        return filter;
    }
    
    private void fireChange() {
        if(valueChangeHandler != null) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @Override
    public void applyBaseFilter(Filter filter) {
        draw(filter);
    }

    @Override
    /** only sets the selection. To (re)draw the widgets based on the possibly new filter,
     * call applyBaseFilter or draw */
    public void setValue(Filter value) {
        setValue(value, false);
    }

    @Override
    /** only sets the selection. To (re)draw the widgets based on the possibly new filter,
     * call applyBaseFilter or draw */
    public void setValue(Filter value, boolean fireEvents) {
        if (value.isRestricted(DIMENSION_TYPE)) {
            Collection<Integer> restriction = value.getRestrictions(DIMENSION_TYPE);
            for (AttributeGroupFilterWidget widget : widgets) {
                widget.setSelection(restriction);
            }
        }
    }

    @Override
    /** not implemented */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Filter> handler) {
        return null;
    }

    @Override
    /** not implemented */
    public void fireEvent(GwtEvent<?> event) {
    }

}
