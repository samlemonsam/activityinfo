package org.activityinfo.store.query.server;

import com.google.common.base.Strings;
import org.activityinfo.model.form.*;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.eval.EvalContext;
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
        FormulaNode filter = parseFilter(record.getFormId(), formPermissions.getFilter(operation));
        return evalFilter(record, filter);
    }

    private boolean evalFilter(FormInstance record, FormulaNode filter) {
        FormClass formClass = formClassProvider.getFormClass(record.getFormId());
        EvalContext context = new FormEvalContext(formClass, record);
        return filter.evaluate(context) == BooleanFieldValue.TRUE;
    }

    private FormulaNode parseFilter(ResourceId formId, String filter) {
        if(Strings.isNullOrEmpty(filter)) {
            return new ConstantNode(true);
        }

        try {
            return FormulaParser.parse(filter);
        } catch (FormulaException e) {
            LOGGER.log(Level.SEVERE,
                String.format("Failed to parse filter '%s' for form %s, falling back to denied access",
                    formId.asString(),
                    formId), e);

            return new ConstantNode(false);
        }
    }

}
