package org.activityinfo.server.command.handler.pivot;

import com.google.common.base.Optional;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.EntityCategory;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.activityinfo.model.legacy.CuidAdapter.*;

public class AdminDimBinding extends DimBinding {
    
    private final AdminDimension model;

    private final String idColumn;
    private final String labelColumn;

    public AdminDimBinding(AdminDimension model) {
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

        ResourceId levelClassId = adminLevelFormClass(model.getLevelId());
        Optional<FormClass> adminClass = formTree.getFormClassIfPresent(levelClassId);
        
        if(adminClass.isPresent()) {
            ColumnModel id = new ColumnModel();
            id.setExpression(new CompoundExpr(levelClassId, ColumnModel.ID_SYMBOL));
            id.setId(idColumn);
            
            ColumnModel label = new ColumnModel();
            label.setExpression(new FieldPath(levelClassId, field(levelClassId, NAME_FIELD)));
            label.setId(labelColumn);
            
            return Arrays.asList(id, label);
            
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public DimensionCategory[] extractCategories(ActivityMetadata activity, ColumnSet columnSet) {
        DimensionCategory[] c = new DimensionCategory[columnSet.getNumRows()];
        if(columnSet.getColumns().containsKey(labelColumn)) {
            ColumnView idView = columnSet.getColumnView(idColumn);
            ColumnView labelView = columnSet.getColumnView(labelColumn);
            
            for (int i = 0; i < columnSet.getNumRows(); i++) {
                String id = idView.getString(i);
                if(id != null) {
                    int entityId = getLegacyIdFromCuid(id);
                    String label = labelView.getString(i);
                    c[i] = new EntityCategory(entityId, label);
                }
            }
        }
        return c;
    }
}
