package org.activityinfo.server.command.handler.pivot;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminAccessor extends DimensionAccessor {
    
    private final AdminDimension model;

    private final String idColumn;
    private final String labelColumn;

    public AdminAccessor(AdminDimension model) {
        this.model = model;
        this.idColumn = "Admin" + model.getLevelId();
        this.labelColumn = "AdminName" + model.getLevelId();
    }

    @Override
    public Dimension getModel() {
        return model;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        
        List<FieldPath> adminField = findAdminField(formTree);
        if(!adminField.isEmpty()) {
            ResourceId levelClassId = formTree.getRootFormClass().getId();

            FieldPath path = Iterables.getOnlyElement(adminField);
            ColumnModel id = new ColumnModel();
            id.setExpression(path);
            id.setId(idColumn);
            
            ColumnModel label = new ColumnModel();
            label.setExpression(new FieldPath(path, CuidAdapter.field(levelClassId, CuidAdapter.NAME_FIELD)));
            label.setId(labelColumn);
            
            return Arrays.asList(id, label);
        } else {
            return Collections.emptyList();
        }
    }

    private List<FieldPath> findAdminField(FormTree formTree) {
        final ResourceId levelClassId = CuidAdapter.adminLevelFormClass(model.getLevelId());
        return formTree.search(FormTree.SearchOrder.BREADTH_FIRST, Predicates.alwaysTrue(), new Predicate<FormTree.Node>() {
            @Override
            public boolean apply(FormTree.Node input) {
                return input.isReference() && input.getRange().contains(levelClassId);
            }
        });
    }

    @Override
    public DimensionCategory[] extractCategories(FormTree formTree, ColumnSet columnSet) {
        DimensionCategory[] c = new DimensionCategory[columnSet.getNumRows()];
        if(columnSet.getColumns().containsKey(idColumn)) {
            ColumnView idView = columnSet.getColumnView(idColumn);
            ColumnView labelView = columnSet.getColumnView(labelColumn);
            
            for (int i = 0; i < columnSet.getNumRows(); i++) {
                String id = idView.getString(i);
                if(id != null) {
                    int entityId = CuidAdapter.getLegacyIdFromCuid(id);
                    String label = labelView.getString(i);
                    c[i] = new EntityCategory(entityId, label);
                }
            }
        }
        return c;
    }
}
