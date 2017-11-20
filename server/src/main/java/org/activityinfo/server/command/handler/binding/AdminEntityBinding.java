package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.expr.SymbolExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.*;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminEntityBinding implements FieldBinding {

    private static final String ADMIN_ENTITY_ID_COLUMN = "adminEntity";
    private static final String ADMIN_ENTITY_NAME_COLUMN = "adminEntityName";

    private FormClass adminForm;
    private FieldBinding locationBinding;

    public AdminEntityBinding(FormClass adminForm) {
        this.adminForm = adminForm;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView adminEntityId = columnSet.getColumnView(ADMIN_ENTITY_ID_COLUMN);
        ColumnView adminEntityName = columnSet.getColumnView(ADMIN_ENTITY_NAME_COLUMN);

        int levelId = CuidAdapter.getLegacyIdFromCuid(adminForm.getId());
        String levelName = adminForm.getLabel();
        FormField parentField = getParentField(adminForm);
        List<AdminEntityDTO> extractedEntities = Lists.newArrayList();

        for (int i=0; i<columnSet.getNumRows(); i++) {
            String entityId = adminEntityId.getString(i);
            if (Strings.isNullOrEmpty(entityId)) {
                continue;
            }
            AdminEntityDTO adminEntity = new AdminEntityDTO(
                    levelId,
                    CuidAdapter.getLegacyIdFromCuid(entityId),
                    adminEntityName.getString(i)
            );
            adminEntity.setLevelName(levelName);
            if (parentField != null) {
                adminEntity.setParentId(CuidAdapter.getLegacyIdFromCuid(parentField.getId()));
            }
            dataArray[i].set(AdminLevelDTO.getPropertyName(adminEntity.getLevelId()),adminEntity);
            extractedEntities.add(adminEntity);
        }

        if (locationBinding != null) {
            locationBinding.extractFieldData(extractedEntities.toArray(new AdminEntityDTO[extractedEntities.size()]), columnSet);
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        List<ColumnModel> adminQuery = Arrays.asList(
                new ColumnModel().setExpression(new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.ID_SYMBOL)).as(ADMIN_ENTITY_ID_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.NAME_SYMBOL)).as(ADMIN_ENTITY_NAME_COLUMN)
        );
        List<ColumnModel> adminLocationQuery = buildAdminLocationQuery(formTree);
        return joinLists(adminQuery,adminLocationQuery);
    }

    private List<ColumnModel> buildAdminLocationQuery(FormTree formTree) {
        try {
            FormField geoField = adminForm.getField(CuidAdapter.field(adminForm.getId(),CuidAdapter.GEOMETRY_FIELD));
            // TODO
            // Disabled for now...
            if (geoField.getType() instanceof GeoPointType) {
                //locationBinding = new GeoPointFieldBinding(geoField);
                //return locationBinding.getColumnQuery(formTree);
            } else if (geoField.getType() instanceof GeoAreaType) {
                //locationBinding = new GeoAreaFieldBinding(adminForm);
                //return locationBinding.getColumnQuery(formTree);
            }
        } catch (IllegalArgumentException excp) { /* ignore and return empty list */ }
        return Lists.newArrayList();
    }

    private List<ColumnModel> joinLists(List<ColumnModel> list1, List<ColumnModel> list2) {
        List<ColumnModel> returnColumns = new ArrayList<>(list1.size()+list2.size());
        returnColumns.addAll(list1);
        returnColumns.addAll(list2);
        return returnColumns;
    }

    private FormField getParentField(FormClass form) {
        try {
            return form.getField(CuidAdapter.field(form.getId(),CuidAdapter.ADMIN_PARENT_FIELD));
        } catch (IllegalArgumentException excp) {
            return null;
        }
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
