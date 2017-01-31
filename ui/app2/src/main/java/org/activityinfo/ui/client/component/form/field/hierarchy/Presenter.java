package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Models the selection of hierarchy
 */
class Presenter {

    private Map<ResourceId, LevelView> widgetMap = new HashMap<>();
    private Map<ResourceId, Choice> selection = new HashMap<>();
    private ResourceLocator locator;
    private Hierarchy tree;
    private ValueUpdater<ReferenceValue> valueUpdater;

    Presenter(ResourceLocator locator, final Hierarchy tree, Map<ResourceId, ? extends LevelView> widgets,
              ValueUpdater<ReferenceValue> valueUpdater) {
        this.locator = locator;
        this.tree = tree;
        this.valueUpdater = valueUpdater;
        this.widgetMap.putAll(widgets);
        for(final Map.Entry<ResourceId, LevelView> entry : widgetMap.entrySet()) {
            entry.getValue().addSelectionHandler(new SelectionHandler<Choice>() {
                @Override
                public void onSelection(SelectionEvent<Choice> event) {
                    onUserSelection(tree.getLevel(entry.getKey()), event.getSelectedItem());
                }
            });
        }
    }


    public void fireValueChanged() {
        valueUpdater.update(getValue());
    }

    public Promise<Void> setInitialSelection(Iterable<RecordRef> refs) {
        return setInitialSelection(new ReferenceValue(refs));
    }

    public Promise<Void> setInitialSelection(ReferenceValue value) {
        final InitialSelection initialSelection = new InitialSelection(tree);
        return initialSelection.fetch(locator, value.getReferences()).then(new Function<Void, Void>() {

            @Nullable
            @Override
            public Void apply(@Nullable Void input) {
                selection.putAll(initialSelection.getSelection());
                for (Level level : tree.getLevels()) {
                    LevelView view = widgetMap.get(level.getFormId());
                    if (level.isRoot() || hasSelection(level.getParent())) {
                        view.setEnabled(true);
                        view.setChoices(choices(level));
                    } else {
                        view.setEnabled(false);
                    }
                    if (hasSelection(level)) {
                        view.setSelection(getSelection(level));
                    }
                }
                return null;
            }
        });
    }


    private void onUserSelection(Level level, Choice selectedItem) {
        if(selectedItem == null) {
            this.selection.remove(level.getFormId());
        } else {
            this.selection.put(level.getFormId(), selectedItem);
        }
        clearChildren(level);
        fireValueChanged();
    }

    private ReferenceValue getValue() {
        // We want to store the values in a normalized fashion -
        // store only the leaf nodes, their parents are redundant
        Set<RecordRef> refs = Sets.newHashSet();
        Set<RecordRef> parentIds = Sets.newHashSet();
        for(Choice choice : selection.values()) {
            refs.add(choice.getRef());
            if(choice.hasParent()) {
                parentIds.add(choice.getParentRef());
            }
        }
        return new ReferenceValue(refs);
    }

    private void clearChildren(Level parent) {
        Choice parentChoice = selection.get(parent.getFormId());
        for(Level child : parent.getChildren()) {
            selection.remove(child.getFormId());
            clearViewSelection(parentChoice, child);
            clearChildren(child);
        }
    }

    private void clearViewSelection(Choice parentSelection, Level child) {
        LevelView view = widgetMap.get(child.getFormId());
        view.clearSelection();
        if(parentSelection != null) {
            view.setChoices(choices(child));
            view.setEnabled(true);
        } else {
            view.setEnabled(false);
        }
    }

    public boolean hasSelection(Level level) {
        return selection.containsKey(level.getFormId());
    }

    public String getSelectionLabel(ResourceId classId) {
        assert selection.containsKey(classId) : "No selection";
        return selection.get(classId).getLabel();
    }

    public Choice getSelection(Level level) {
        assert selection.containsKey(level.getFormId());
        return selection.get(level.getFormId());
    }
    
    private Supplier<Promise<List<Choice>>> choices(final Level level) {

        final QueryModel queryModel = new QueryModel(level.getFormId());
        queryModel.selectResourceId().as("id");
        queryModel.selectExpr("label").as("label");

        if(!level.isRoot()) {
            Choice selectedParent = getSelection(level.getParent());
            queryModel.selectExpr("parent").as("parent");
            queryModel.setFilter(Exprs.equals(new SymbolExpr("parent"), Exprs.idConstant(selectedParent.getRef().getRecordId())));
        }

        return new Supplier<Promise<List<Choice>>>() {
            @Override
            public Promise<List<Choice>> get() {

                return locator.queryTable(queryModel).then(new Function<ColumnSet, List<Choice>>() {
                    @Nullable
                    @Override
                    public List<Choice> apply(ColumnSet input) {
                        ColumnView id = input.getColumnView("id");
                        ColumnView label = input.getColumnView("label");
                        ColumnView parent = input.getColumnView("parent");

                        List<Choice> choices = new ArrayList<>();
                        for (int i = 0; i < input.getNumRows(); i++) {
                            if(parent == null) {

                               choices.add(new Choice(level.getFormId(),
                                        ResourceId.valueOf(id.getString(i)),
                                        label.getString(i)));
                            } else {
                                choices.add(new Choice(level.getFormId(),
                                        ResourceId.valueOf(id.getString(i)),
                                        label.getString(i),
                                        new RecordRef(level.getParent().getFormId(), ResourceId.valueOf(parent.getString(i)))));
                            }
                        }
                        return choices;
                    }
                });
            }
        };
    }
}
