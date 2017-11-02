package org.activityinfo.server.command.handler.binding;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;
import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

import java.util.Arrays;
import java.util.List;

public class AdminEntityBinding implements FieldBinding {

    private static final String ADMIN_ENTITY_ID_COLUMN = "adminEntity";
    private static final String ADMIN_ENTITY_NAME_COLUMN = "adminEntityName";
    private static final String ADMIN_LEVEL_ID_COLUMN = "adminLevelId";
    private static final String ADMIN_LEVEL_NAME_COLUMN = "adminLevelName";
    private static final String ADMIN_LEVEL_PARENT_COLUMN = "adminLevelParent";

    private FormClass adminForm;

    public AdminEntityBinding(FormClass adminForm) {
        this.adminForm = adminForm;
    }

    @Override
    public BaseModelData[] extractFieldData(BaseModelData[] dataArray, ColumnSet columnSet) {
        ColumnView adminEntityId = columnSet.getColumnView(ADMIN_ENTITY_ID_COLUMN);
        ColumnView adminEntityName = columnSet.getColumnView(ADMIN_ENTITY_NAME_COLUMN);

        for (int i=0; i<columnSet.getNumRows(); i++) {
            AdminEntityDTO adminEntity = new AdminEntityDTO(
                    CuidAdapter.getLegacyIdFromCuid(adminForm.getId()),
                    CuidAdapter.getLegacyIdFromCuid(adminEntityId.getString(i)),
                    adminEntityName.getString(i)
            );
            adminEntity.setLevelName(adminForm.getLabel());

            FormField parentField = getParentField(adminForm);
            if (parentField != null) {
                adminEntity.setParentId(CuidAdapter.getLegacyIdFromCuid(parentField.getId()));
            }

            dataArray[i].set(AdminLevelDTO.getPropertyName(adminEntity.getLevelId()),adminEntity);
        }

        return dataArray;
    }

    @Override
    public List<ColumnModel> getColumnQuery(FormTree formTree) {
        List<ColumnModel> adminQuery = Arrays.asList(
                new ColumnModel().setExpression(new CompoundExpr(adminForm.getId(),LocationFieldBinding.ID_SYMBOL)).as(ADMIN_ENTITY_ID_COLUMN),
                new ColumnModel().setExpression(new CompoundExpr(adminForm.getId(), LocationFieldBinding.NAME_SYMBOL)).as(ADMIN_ENTITY_NAME_COLUMN)
        );
        return adminQuery;
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
