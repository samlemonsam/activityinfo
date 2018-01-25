package org.activityinfo.store.query.server;

import com.google.common.base.Strings;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.expr.diagnostic.ExprException;
import org.activityinfo.model.expr.eval.EvalContext;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.store.query.shared.FormSupervisor;
import org.activityinfo.store.spi.FormStorageProvider;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PermissionsEnforcer {

    private static final Logger LOGGER = Logger.getLogger(PermissionsEnforcer.class.getName());

    private final FormSupervisor supervisor;
    private final FormClassProvider formClassProvider;

    public PermissionsEnforcer(FormStorageProvider catalog, int userId) {
        this(new FormSupervisorAdapter(catalog, userId), catalog);
    }

    public PermissionsEnforcer(FormSupervisor supervisor, FormClassProvider formClassProvider) {
        this.supervisor = supervisor;
        this.formClassProvider = formClassProvider;
    }


    public boolean canEdit(FormInstance record) {
        return can(record, FormOperation.EDIT_RECORD);
    }

    public boolean canView(FormInstance record) {
        return can(record, FormOperation.VIEW);
    }

    public boolean canView(FormRecord record) {
        FormClass formClass = formClassProvider.getFormClass(ResourceId.valueOf(record.getFormId()));
        return canView(FormInstance.toFormInstance(formClass, record));
    }

    public boolean can(FormInstance record, FormOperation operation) {
        FormPermissions formPermissions = supervisor.getFormPermissions(record.getFormId());
        if(!formPermissions.isAllowed(operation)) {
            return false;
        }
        if(!formPermissions.isFiltered(operation)) {
            return true;
        }
        ExprNode filter = parseFilter(record.getFormId(), formPermissions.getFilter(operation));
        return evalFilter(record, filter);
    }

    private boolean evalFilter(FormInstance record, ExprNode filter) {
        FormClass formClass = formClassProvider.getFormClass(record.getFormId());
        EvalContext context = new FormEvalContext(formClass, record);
        return filter.evaluate(context) == BooleanFieldValue.TRUE;
    }

    private ExprNode parseFilter(ResourceId formId, String filter) {
        if(Strings.isNullOrEmpty(filter)) {
            return new ConstantExpr(true);
        }

        try {
            return ExprParser.parse(filter);
        } catch (ExprException e) {
            LOGGER.log(Level.SEVERE,
                String.format("Failed to parse filter '%s' for form %s, falling back to denied access",
                    formId.asString(),
                    formId), e);

            return new ConstantExpr(false);
        }
    }

}
