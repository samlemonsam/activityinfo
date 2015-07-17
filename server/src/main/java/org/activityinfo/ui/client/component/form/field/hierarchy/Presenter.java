package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.application.ApplicationProperties;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.CriteriaIntersection;
import org.activityinfo.core.shared.criteria.FieldCriteria;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Models the selection of hierarchy
 */
class Presenter {
    private Map<ResourceId, LevelView> widgetMap = new HashMap<>();
    private Map<ResourceId, Projection> selection = new HashMap<>();
    private List<HandlerRegistration> registrations = new ArrayList<>();
    private ResourceLocator locator;
    private Hierarchy tree;
    private ValueUpdater valueUpdater;

    Presenter(ResourceLocator locator, final Hierarchy tree, Map<ResourceId, ? extends LevelView> widgets,
              ValueUpdater valueUpdater) {
        this.locator = locator;
        this.tree = tree;
        this.valueUpdater = valueUpdater;
        this.widgetMap.putAll(widgets);
        for(final Map.Entry<ResourceId, LevelView> entry : widgetMap.entrySet()) {
            entry.getValue().addSelectionHandler(new SelectionHandler<Projection>() {
                @Override
                public void onSelection(SelectionEvent<Projection> event) {
                    onUserSelection(tree.getLevel(entry.getKey()), event.getSelectedItem());
                }
            });
        }
    }

    public Promise<Void> setInitialSelection(Iterable<ResourceId> resourceIds) {
        return setInitialSelection(new ReferenceValue(resourceIds));
    }

    public Promise<Void> setInitialSelection(ReferenceValue value) {
        final InitialSelection initialSelection = new InitialSelection(tree);
        return initialSelection.fetch(locator, value.getResourceIds()).then(new Function<Void, Void>() {

            @Nullable
            @Override
            public Void apply(@Nullable Void input) {
                selection.putAll(initialSelection.getSelection());
                for(Level level : tree.getLevels()) {
                    LevelView view = widgetMap.get(level.getClassId());
                    if(level.isRoot() || hasSelection(level.getParent())) {
                        view.setEnabled(true);
                        view.setChoices(choices(level));
                    } else {
                        view.setEnabled(false);
                    }
                    if(hasSelection(level)) {
                        view.setSelection(getSelection(level));
                    }
                }
                return null;
            }
        });
    }


    private void onUserSelection(Level level, Projection selectedItem) {
        if(selectedItem == null) {
            this.selection.remove(level.getClassId());
        } else {
            this.selection.put(level.getClassId(), selectedItem);
        }
        clearChildren(level);
        valueUpdater.update(getValue());
    }

    private ReferenceValue getValue() {
        // We want to store the values in a normalized fashion -
        // store only the leaf nodes, their parents are redundant
        Set<ResourceId> instanceIds = Sets.newHashSet();
        Set<ResourceId> parentIds = Sets.newHashSet();
        for(Projection projection : selection.values()) {
            instanceIds.add(projection.getRootInstanceId());
            Set<ResourceId> parentId = projection.getReferenceValue(ApplicationProperties.PARENT_PROPERTY);
            if(!parentId.isEmpty()) {
                parentIds.add(parentId.iterator().next());
            }
        }
        return new ReferenceValue(instanceIds);
    }

    private void clearChildren(Level parent) {
        Projection parentSelection = selection.get(parent.getClassId());
        for(Level child : parent.getChildren()) {
            selection.remove(child.getClassId());
            clearViewSelection(parentSelection, child);
            clearChildren(child);
        }
    }

    private void clearViewSelection(Projection parentSelection, Level child) {
        LevelView view = widgetMap.get(child.getClassId());
        view.clearSelection();
        if(parentSelection != null) {
            view.setChoices(choices(child));
            view.setEnabled(true);
        } else {
            view.setEnabled(false);
        }
    }

    public boolean hasSelection(Level level) {
        return selection.containsKey(level.getClassId());
    }

    public String getSelectionLabel(ResourceId classId) {
        assert selection.containsKey(classId) : "No selection";
        return selection.get(classId).getStringValue(ApplicationProperties.LABEL_PROPERTY);
    }

    public Projection getSelection(Level level) {
        assert selection.containsKey(level.getClassId());
        return selection.get(level.getClassId());
    }

    private Supplier<Promise<List<Projection>>> choices(Level level) {
        final InstanceQuery.Builder query = InstanceQuery
                .select(ApplicationProperties.LABEL_PROPERTY, ApplicationProperties.PARENT_PROPERTY,
                        level.getParentFieldId());

        if(level.isRoot()) {
            query.where(new ClassCriteria(level.getClassId()));
        } else {
            Projection selectedParent = getSelection(level.getParent());

            query.where(new CriteriaIntersection(
                    new ClassCriteria(level.getClassId()),
                    new FieldCriteria(level.getParentFieldId(), new ReferenceValue(selectedParent.getRootInstanceId()))));
        }

        return new Supplier<Promise<List<Projection>>>() {
            @Override
            public Promise<List<Projection>> get() {
                return locator.query(query.build());
            }
        };
    }
}
