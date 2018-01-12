package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.common.base.Optional;
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

import java.util.Arrays;
import java.util.List;

public class AdminEntityBinding implements FieldBinding {

    private final CompoundExpr ADMIN_ENTITY_ID_COLUMN;
    private final CompoundExpr ADMIN_ENTITY_NAME_COLUMN;

    private final int adminLevel;
    private final String adminLevelName;
    private final ResourceId adminLevelParentId;

    public AdminEntityBinding(FormClass adminForm) {
        this.adminLevel = CuidAdapter.getLegacyIdFromCuid(adminForm.getId());
        this.adminLevelName = adminForm.getLabel();
        this.adminLevelParentId = getParentId(adminForm);

        this.ADMIN_ENTITY_ID_COLUMN =new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.ID_SYMBOL);
        this.ADMIN_ENTITY_NAME_COLUMN = new CompoundExpr(new SymbolExpr(adminForm.getId()), LocationFieldBinding.NAME_SYMBOL);
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView adminEntityId = columnSet.getColumnView(ADMIN_ENTITY_ID_COLUMN.asExpression());
        ColumnView adminEntityName = columnSet.getColumnView(ADMIN_ENTITY_NAME_COLUMN.asExpression());

        for (int i = 0; i < columnSet.getNumRows(); i++) {
            if (adminEntityId.isMissing(i)) {
                continue;
            }

            AdminEntityDTO adminEntity = new AdminEntityDTO(
                    adminLevel,
                    CuidAdapter.getLegacyIdFromCuid(adminEntityId.getString(i)),
                    adminEntityName.getString(i)
            );

            adminEntity.setLevelName(adminLevelName);

            if (adminLevelParentId != null) {
                adminEntity.setParentId(CuidAdapter.getLegacyIdFromCuid(adminLevelParentId));
            }

            dataArray[i].set(AdminLevelDTO.getPropertyName(adminEntity.getLevelId()), adminEntity);
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        return getAdminEntityQuery();
    }

    @Override
    public List<ColumnModel> getTargetColumnQuery(ResourceId targetFormId) {
        return getAdminEntityQuery();
    }

    private List<ColumnModel> getAdminEntityQuery() {
        // TODO: AdminEntity location extraction disabled until "ST__" functions corrected on QueryEngine
        return Arrays.asList(
                new ColumnModel().setExpression(ADMIN_ENTITY_ID_COLUMN).as(ADMIN_ENTITY_ID_COLUMN.asExpression()),
                new ColumnModel().setExpression(ADMIN_ENTITY_NAME_COLUMN).as(ADMIN_ENTITY_NAME_COLUMN.asExpression())
        );
    }

    private ResourceId getParentId(FormClass form) {
        ResourceId parentFieldId = CuidAdapter.field(form.getId(), CuidAdapter.ADMIN_PARENT_FIELD);
        Optional<FormField> potentialParentField = form.getFieldIfPresent(parentFieldId);
        if (potentialParentField.isPresent()) {
            FormField parentField = potentialParentField.get();
            ReferenceType parentFieldType = (ReferenceType) parentField.getType();
            return parentFieldType.getRange().iterator().next();
        } else {
            return null;
        }
    }

}
