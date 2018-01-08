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
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminEntityBinding implements FieldBinding {

    private CompoundExpr ADMIN_ENTITY_ID_COLUMN;
    private CompoundExpr ADMIN_ENTITY_NAME_COLUMN;

    private FormClass adminForm;
    private FieldBinding locationBinding;

    public AdminEntityBinding(FormClass adminForm) {
        this.adminForm = adminForm;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView adminEntityId = columnSet.getColumnView(ADMIN_ENTITY_ID_COLUMN.asExpression());
        ColumnView adminEntityName = columnSet.getColumnView(ADMIN_ENTITY_NAME_COLUMN.asExpression());

        int levelId = CuidAdapter.getLegacyIdFromCuid(adminForm.getId());
        String levelName = adminForm.getLabel();
        ResourceId parentRef = getParentReference(adminForm);
        List<AdminEntityDTO> extractedEntities = Lists.newArrayList();

        for (int i = 0; i < columnSet.getNumRows(); i++) {
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

            if (parentRef != null) {
                adminEntity.setParentId(CuidAdapter.getLegacyIdFromCuid(parentRef));
            }

            dataArray[i].set(AdminLevelDTO.getPropertyName(adminEntity.getLevelId()), adminEntity);
            extractedEntities.add(adminEntity);
        }

        // TODO: Location extraction disabled until "ST_" functions corrected on QueryEngine

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        ADMIN_ENTITY_ID_COLUMN = new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.ID_SYMBOL);
        ADMIN_ENTITY_NAME_COLUMN = new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.NAME_SYMBOL);

        List<ColumnModel> adminQuery = Arrays.asList(
                new ColumnModel().setExpression(ADMIN_ENTITY_ID_COLUMN).as(ADMIN_ENTITY_ID_COLUMN.asExpression()),
                new ColumnModel().setExpression(ADMIN_ENTITY_NAME_COLUMN).as(ADMIN_ENTITY_NAME_COLUMN.asExpression())
        );
        List<ColumnModel> adminLocationQuery = buildAdminLocationQuery(formTree);

        return joinLists(adminQuery,adminLocationQuery);
    }

    private List<ColumnModel> buildAdminLocationQuery(FormTree formTree) {
        try {
            FormField geoField = adminForm.getField(CuidAdapter.field(adminForm.getId(), CuidAdapter.GEOMETRY_FIELD));
            if (geoField.getType() instanceof GeoAreaType) {
                // TODO: Disabled until "ST_" functions corrected on QueryEngine
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

    private ResourceId getParentReference(FormClass form) {
        try {
            FormField parentField = form.getField(CuidAdapter.field(form.getId(),CuidAdapter.ADMIN_PARENT_FIELD));
            if (parentField.getType() instanceof ReferenceType) {
                ReferenceType parentRefType = (ReferenceType) parentField.getType();
                return parentRefType.getRange().iterator().next();
            }
        } catch (IllegalArgumentException excp) { /* Do nothing and return null reference */ }
        return null;
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return null;
    }
}
