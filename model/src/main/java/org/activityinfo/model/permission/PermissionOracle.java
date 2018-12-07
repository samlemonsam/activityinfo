package org.activityinfo.model.permission;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.BooleanFieldValue;

import java.util.Optional;

public class PermissionOracle {

    private PermissionOracle() {
    }

    public static Permission query(PermissionQuery query, UserDatabaseMeta db) {
        if (db.isOwner()) {
            return allowOwner(query.getOperation());
        }
        if (db.isPublished()) {
            return allow(query.getOperation());
        }
        if (!db.isVisible()) {
            return deny(query.getOperation());
        }
        // Deny permission if resource is not specially handled and is not present in resource list
        if (!isSpecialResource(query.getResourceId()) && !db.hasResource(query.getResourceId())) {
            return deny(query.getOperation());
        }
        // Otherwise, continue with permission determination
        return determinePermission(query.getOperation(), query.getResourceId(), db);
    }

    private static boolean isSpecialResource(ResourceId resourceId) {
        return isDatabase(resourceId)
                || isPartnerForm(resourceId)
                || isProjectForm(resourceId)
                || isAdminLevelForm(resourceId);
    }

    /**
     * Allow the owner of a database full permissions with no record filters
     */
    private static Permission allowOwner(Operation operation) {
        return new Permission(operation, true, Optional.empty());
    }

    /**
     * Allow the current user view permissions only with no record filters
     */
    private static Permission allowViewOnly(Operation operation) {
        if (operation != Operation.VIEW) {
            return deny(operation);
        }
        return allow(operation);
    }

    /**
     * Allow permission outright for the specified operation, with no filter
     */
    private static Permission allow(Operation operation) {
        return new Permission(operation, true, Optional.empty());
    }


    /**
     * Deny permission outright for the specified operation
     */
    private static Permission deny(Operation operation) {
        return new Permission(operation, false, Optional.empty());
    }

    /**
     * <p> A user can perform a given {@link Operation} on a {@link Resource} if they have an explicit grant for the
     * {@code operation} on this {@code resource} or on the <b>closest</b> parent {@code resource}</p>
     *
     * <p> A user may also be limited in the records available to view, defined by a record filter composed of the
     * filters applied at each level of the resource tree for this operation. </p>
     */
    private static Permission determinePermission(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        Permission permission = new Permission(operation);
        permission.setPermitted(operationPermitted(operation, resourceId, db));
        if (permission.isPermitted()) {
            permission.setFilter(operationFilter(operation, resourceId, db));
        }
        return permission;
    }

    /**
     * If user is requesting permission on a database, need to check specified database is the provided
     * {@link UserDatabaseMeta} and whether the specified operation is permitted.
     *
     * Otherwise, we ensure that the specified resource is visible and then check whether the specified operation
     * is permitted.
     */
    private static boolean operationPermitted(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        if (isDatabase(resourceId)) {
            return db.getDatabaseId().equals(resourceId)
                    && db.hasGrant(resourceId)
                    && db.getGrant(resourceId).get().hasOperation(operation);
        } else if (isPartnerForm(resourceId)) {
            return allowedPartnerOperation(operation, db);
        } else if (isProjectForm(resourceId)) {
            return allowedProjectOperation(resourceId, operation, db);
        } else if (isAdminLevelForm(resourceId)) {
            return allowedAdminLevelOperation(operation, db);
        } else {
            return db.hasResource(resourceId)
                    && granted(operation, db.getResource(resourceId).get(), db);
        }
    }

    private static boolean allowedPartnerOperation(Operation operation, UserDatabaseMeta db) {
        Optional<GrantModel> databaseGrant = findDatabaseGrantIfPresent(db);
        switch(operation) {
            case VIEW:
                return db.isVisible();
            case MANAGE_USERS:
                return databaseGrant.isPresent() && databaseGrant.get().hasOperation(operation);
            case CREATE_RECORD:
            case EDIT_RECORD:
            case DELETE_RECORD:
            case IMPORT_RECORDS:
            case EXPORT_RECORDS:
                return databaseGrant.isPresent()
                        && databaseGrant.get().hasOperation(Operation.MANAGE_USERS)
                        && databaseGrant.get().hasOperation(operation);
            default:
                return false;
        }
    }

    private static boolean allowedProjectOperation(ResourceId projectFormId, Operation operation, UserDatabaseMeta db) {
        switch(operation) {
            case VIEW:
                return db.isVisible();
            case CREATE_RECORD:
            case EDIT_RECORD:
            case DELETE_RECORD:
            case IMPORT_RECORDS:
            case EXPORT_RECORDS:
                return canEditResource(projectFormId, db);
            default:
                return false;
        }
    }

    private static Optional<GrantModel> findDatabaseGrantIfPresent(UserDatabaseMeta db) {
        if (!db.isVisible()) {
            return Optional.empty();
        }
        if (!db.hasGrant(db.getDatabaseId())) {
            return Optional.empty();
        }
        return db.getGrant(db.getDatabaseId());
    }

    private static boolean allowedAdminLevelOperation(Operation operation, UserDatabaseMeta db) {
        return db.isVisible() && Operation.VIEW.equals(operation);
    }

    /**
     * Checks whether the specified {@link Operation} has been granted on the given {@link Resource}, or on the
     * <b>closest</b> parent resource with an explicit grant
     */
    private static boolean granted(Operation operation, Resource resource, UserDatabaseMeta db) {
        // If there is an explicit grant, check whether the operation is granted at this level
        if (db.hasGrant(resource.getId())) {
            return db.getGrant(resource.getId()).get().hasOperation(operation);
        }

        // If there is no explicit grant:
        // 1. Check for VIEW operation requests on Resources which are Public
        if (Operation.VIEW.equals(operation) && resource.isPublic()) {
            return true;
        }
        // 2. Check for VIEW operation requests on Resources which are Public to Database Users
        if (Operation.VIEW.equals(operation) && resource.isPublicToDatabaseUsers() && db.isVisible()) {
            return true;
        }
        // 3. Check further up the Resource tree:
        // -> If the parent of this resource is the root database, then check whether operation exists on database grant
        if (isDatabase(resource.getParentId())) {
            return db.hasGrant(resource.getParentId())
                    && db.getGrant(resource.getParentId()).get().hasOperation(operation);
        }
        // -> Otherwise, we climb the resource tree to determine whether the operation is granted there
        return granted(operation, db.getResource(resource.getParentId()).get(), db);
    }

    private static Optional<String> operationFilter(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        if (isDatabase(resourceId)) {
            return getFilter(operation, resourceId, db);
        } else if (isPartnerForm(resourceId)) {
            return getFilter(Operation.MANAGE_USERS, db.getDatabaseId(), db);
        } else if (isProjectForm(resourceId)) {
            return Optional.empty();
        } else if (isAdminLevelForm(resourceId)) {
            return Optional.empty();
        } else {
            return collectFilters(operation, db.getResource(resourceId).get(), db);
        }
    }

    /**
     * Concatenates all filters defined for the given operation at each level of the resource tree.
     *
     * Filters defined on different levels for the same operation imply an AND relationship. E.g. A user is given
     * permission to only view a certain partner across a database, but is also restricted to only view records from
     * a certain location within a folder. This is equivalent to setting a record-level filter of:
     *      partner==pXXX && location.name=="Gaza"
     *
     * This relationship must be reflected in the returned filter by ANDing filters from different levels.
     */
    private static Optional<String> collectFilters(Operation operation, Resource resource, UserDatabaseMeta db) {
        // Get the filter (if any) for operations granted on this level
        Optional<String> filter = getFilter(operation, resource.getId(), db);
        if (isDatabase(resource.getParentId())) {
            Optional<String> dbFilter = getFilter(operation, resource.getParentId(), db);
            return and(filter, dbFilter);
        }
        return and(filter, collectFilters(operation, db.getResource(resource.getParentId()).get(), db));
    }

    private static Optional<String> getFilter(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        Optional<String> filter = Optional.empty();
        if (db.hasGrant(resourceId) && db.getGrant(resourceId).get().hasOperation(operation)) {
            filter = db.getGrant(resourceId).get().getFilter(operation);
        }
        return filter;
    }

    private static Optional<String> and(Optional<String> filter1, Optional<String> filter2) {
        if (!filter1.isPresent() && !filter2.isPresent()) {
            return Optional.empty();
        } else if (!filter1.isPresent()) {
            return filter2;
        } else if (!filter2.isPresent()) {
            return filter1;
        }
        FormulaNode filterFormula1 = FormulaParser.parse(filter1.get());
        FormulaNode filterFormula2 = FormulaParser.parse(filter2.get());
        if (filterFormula1.equals(filterFormula2)) {
            return filter1;
        }
        FormulaNode and = Formulas.allTrue(Lists.newArrayList(filterFormula1, filterFormula2));
        return Optional.of(and.asExpression());
    }

    ////////////////////////////////////////// RESOURCE DOMAIN CHECK METHODS ///////////////////////////////////////////

    private static boolean isDatabase(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.DATABASE_DOMAIN;
    }

    private static boolean isPartnerForm(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.PARTNER_FORM_CLASS_DOMAIN;
    }

    private static boolean isProjectForm(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN;
    }

    private static boolean isAdminLevelForm(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN;
    }

    ///////////////////////////////////////////// FORM PERMISSION METHODS //////////////////////////////////////////////

    public static FormPermissions formPermissions(ResourceId formId, UserDatabaseMeta db) {
        if (!db.isVisible()) {
            return FormPermissions.none();
        }
        if (db.isOwner()) {
            return FormPermissions.owner();
        }
        if (db.isPublished()) {
            return FormPermissions.readWrite();
        }
        if (isPartnerForm(formId) || isProjectForm(formId)) {
            return computeFormPermissions(formId, db);
        }
        if (!db.hasResource(formId)) {
            return FormPermissions.none();
        }
        if (!isFormOrSubFormResource(db.getResource(formId).get())) {
            return FormPermissions.none();
        }
        return computeFormPermissions(formId, db);
    }

    private static boolean isFormOrSubFormResource(Resource resource) {
        switch(resource.getType()) {
            case FORM:
            case SUB_FORM:
                return true;
            default:
                return false;
        }
    }

    private static FormPermissions computeFormPermissions(ResourceId formId, UserDatabaseMeta db) {
        FormPermissions.Builder permissionsBuilder = new FormPermissions.Builder();
        computeViewFormPermissions(permissionsBuilder, formId, db);
        if (!permissionsBuilder.isAllowedView()) {
            return FormPermissions.none();
        }
        computeEditSchemaFormPermissions(permissionsBuilder, formId, db);
        computeEditRecordFormPermissions(permissionsBuilder, formId, db);
        return permissionsBuilder.build();
    }

    private static void computeViewFormPermissions(FormPermissions.Builder builder,
                                                   ResourceId formId,
                                                   UserDatabaseMeta db) {
        Permission view = view(formId, db);
        if (view.isForbidden()) {
            builder.forbidView();
            return;
        }

        if (view.isFiltered()) {
            builder.allowFilteredView(view.getFilter());
        } else {
            builder.allowView();
        }
    }

    private static void computeEditSchemaFormPermissions(FormPermissions.Builder builder,
                                                         ResourceId formId,
                                                         UserDatabaseMeta db) {
        Permission editFormSchema = editResource(formId, db);
        if (editFormSchema.isPermitted()) {
            builder.allowSchemaUpdate();
        }
    }

    private static void computeEditRecordFormPermissions(FormPermissions.Builder builder,
                                                         ResourceId formId,
                                                         UserDatabaseMeta db) {
        // Legacy "Edit" permission requires CREATE_RECORD, EDIT_RECORD, DELETE_RECORD permissions on form
        Permission createRecord = createRecord(formId, db);
        Permission editRecord = editRecord(formId, db);
        Permission deleteRecord = deleteRecord(formId, db);

        if (createRecord.isForbidden() || editRecord.isForbidden() || deleteRecord.isForbidden()) {
            return;
        }

        if (createRecord.isFiltered() || editRecord.isFiltered() || deleteRecord.isFiltered()) {
            Optional<String> filter = and(
                    and(createRecord.getOptionalFilter(), editRecord.getOptionalFilter()),
                    deleteRecord.getOptionalFilter());
            builder.allowFilteredEdit(filter.get());
        } else {
            builder.allowEdit();
        }
    }

    /////////////////////////////////////////////// FORM INSTANCE METHODS ////////////////////////////////////////////////

    public static boolean canEdit(TypedFormRecord record,
                                  FormPermissions formPermissions,
                                  FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.EDIT_RECORD);
    }

    public static boolean canView(TypedFormRecord record,
                                  FormPermissions formPermissions,
                                  FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.VIEW);
    }

    public static boolean canView(FormRecord record,
                                  FormPermissions formPermissions,
                                  FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.VIEW);
    }

    public static boolean can(FormRecord record,
                              FormPermissions formPermissions,
                              FormClass formClass,
                              Operation operation) {
        return can(TypedFormRecord.toTypedFormRecord(formClass, record), formPermissions, formClass, operation);
    }

    public static boolean can(TypedFormRecord record,
                              FormPermissions formPermissions,
                              FormClass formClass,
                              Operation operation) {
        if (!formPermissions.isAllowed(operation)) {
            return false;
        }
        if (!formPermissions.isFiltered(operation)) {
            return true;
        }
        FormulaNode filter = parseFilter(formPermissions.getFilter(operation));
        return evalFilter(record, formClass, filter);
    }

    private static FormulaNode parseFilter(String filter) {
        if(Strings.isNullOrEmpty(filter)) {
            return new ConstantNode(true);
        }
        try {
            return FormulaParser.parse(filter);
        } catch (FormulaException e) {
            // Failed to parse filter, falling back to denied access
            return new ConstantNode(false);
        }
    }

    private static boolean evalFilter(TypedFormRecord record, FormClass formClass, FormulaNode filter) {
        EvalContext context = new FormEvalContext(formClass, record);
        return filter.evaluate(context) == BooleanFieldValue.TRUE;
    }

    /////////////////////////////////////////////////// UTIL METHODS ///////////////////////////////////////////////////

    public static boolean filterContainsPartner(String filter, ResourceId partnerFormId, ResourceId partnerId) {
        FormulaNode filterFormula = FormulaParser.parse(filter);

        SymbolNode expectedPartnerForm = new SymbolNode(partnerFormId);
        ConstantNode expectedPartnerRecord = new ConstantNode(partnerId.asString());

        if (!(filterFormula instanceof FunctionCallNode)) {
            return false;
        }
        if (!(((FunctionCallNode) filterFormula).getFunction() instanceof EqualFunction)) {
            return false;
        }
        if (((FunctionCallNode) filterFormula).getArgumentCount() != 2) {
            return false;
        }

        FunctionCallNode equalFunctionCall = (FunctionCallNode) filterFormula;

        if (!(equalFunctionCall.getArgument(0 ) instanceof SymbolNode)) {
            return false;
        }
        if (!(equalFunctionCall.getArgument(1) instanceof ConstantNode)) {
            return false;
        }

        SymbolNode partnerFormNode = (SymbolNode) equalFunctionCall.getArgument(0);
        ConstantNode partnerFieldNode = (ConstantNode) equalFunctionCall.getArgument(1);

        if (!partnerFormNode.equals(expectedPartnerForm)) {
            return false;
        }
        if (!partnerFieldNode.equals(expectedPartnerRecord)) {
            return false;
        }

        return true;
    }

    /////////////////////////////////////////////////// TASK METHODS ///////////////////////////////////////////////////

    public static boolean canDeleteDatabase(UserDatabaseMeta db) {
        return db.isOwner();
    }

    public static boolean canEditDatabase(UserDatabaseMeta db) {
        return db.isOwner();
    }

    public static Permission manageUsers(ResourceId resourceId, UserDatabaseMeta db) {
         PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.MANAGE_USERS,
                resourceId);
        return query(query, db);
    }

    public static boolean canManageUsers(UserDatabaseMeta db) {
        return manageUsers(db.getDatabaseId(),db).isPermitted();
    }

    public static boolean canManageUsers(ResourceId resourceId, UserDatabaseMeta db) {
        return manageUsers(resourceId,db).isPermitted();
    }

    public static Permission view(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.VIEW,
                resourceId);
        return query(query, db);
    }

    public static boolean canView(UserDatabaseMeta db) {
        return view(db.getDatabaseId(),db).isPermitted();
    }

    public static boolean canView(ResourceId resourceId, UserDatabaseMeta db) {
        return view(resourceId,db).isPermitted();
    }

    public static boolean canViewSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission view = view(activityId, db);

        if (view.isPermitted() && !view.isFiltered()) {
            return true;
        } else if (view.isPermitted()){
            return PermissionOracle.filterContainsPartner(view.getFilter(),
                    CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                    CuidAdapter.partnerRecordId(partnerId));
        } else {
            return false;
        }
    }

    public static Permission createRecord(ResourceId formId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.CREATE_RECORD,
                formId);
        return query(query, db);
    }

    public static Permission editRecord(ResourceId formId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.EDIT_RECORD,
                formId);
        return query(query, db);
    }

    public static Permission deleteRecord(ResourceId formId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.DELETE_RECORD,
                formId);
        return query(query, db);
    }

    public static boolean canDeleteSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission delete = deleteRecord(activityId, db);

        if (delete.isPermitted() && !delete.isFiltered()) {
            return true;
        } else if (delete.isPermitted()){
            return PermissionOracle.filterContainsPartner(delete.getFilter(),
                    CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                    CuidAdapter.partnerRecordId(partnerId));
        } else {
            return false;
        }
    }

    public static Permission deleteResource(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.DELETE_FORM,
                resourceId);
        return query(query, db);
    }

    public static Permission editResource(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.EDIT_FORM,
                resourceId);
        return query(query, db);
    }

    public static boolean canEditResource(ResourceId resourceId, UserDatabaseMeta db) {
        return editResource(resourceId,db).isPermitted();
    }

    public static boolean canEditSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.EDIT_RECORD,
                activityId);
        Permission edit = PermissionOracle.query(query, db);

        if (edit.isPermitted() && !edit.isFiltered()) {
            return true;
        } else if (edit.isPermitted()){
            return PermissionOracle.filterContainsPartner(edit.getFilter(),
                    CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                    CuidAdapter.partnerRecordId(partnerId));
        } else {
            return false;
        }
    }

    public static boolean canCreateSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                CuidAdapter.getLegacyIdFromCuid(db.getDatabaseId()),
                Operation.CREATE_RECORD,
                activityId);
        Permission create = PermissionOracle.query(query, db);

        if (create.isPermitted() && !create.isFiltered()) {
            return true;
        } else if (create.isPermitted()){
            return PermissionOracle.filterContainsPartner(create.getFilter(),
                    CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                    CuidAdapter.partnerRecordId(partnerId));
        } else {
            return false;
        }
    }

    public static Permission lockRecords(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.LOCK_RECORDS,
                resourceId);
        return query(query, db);
    }

    public static boolean canLockRecords(ResourceId resourceId, UserDatabaseMeta db) {
        return lockRecords(resourceId,db).isPermitted();
    }

    public static Permission createForm(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.CREATE_FORM,
                resourceId);
        return query(query, db);
    }

    public static boolean canCreateForm(ResourceId resoruceId, UserDatabaseMeta db) {
        return createForm(resoruceId,db).isPermitted();
    }

    public static boolean canEditForm(ResourceId formId, UserDatabaseMeta db) {
        return editResource(formId,db).isPermitted();
    }

    public static boolean canEditFolder(ResourceId folderId, UserDatabaseMeta db) {
        return editResource(folderId,db).isPermitted();
    }

    public static boolean canDeleteForm(ResourceId formId, UserDatabaseMeta db) {
        return deleteResource(formId,db).isPermitted();
    }

    public static boolean canDeleteFolder(ResourceId folderId, UserDatabaseMeta db) {
        return deleteResource(folderId,db).isPermitted();
    }

    public static Permission manageTargets(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.MANAGE_TARGETS,
                resourceId);
        return query(query, db);
    }

    public static boolean canManageTargets(ResourceId resourceId, UserDatabaseMeta db) {
        return manageTargets(resourceId,db).isPermitted();
    }

    /**
     * This remains solely for the AuthorizationHandler. Should use correct task method instead.
     */
    public static boolean canDesign(UserDatabaseMeta db) {
        // Legacy Design requires CREATE_FORM, EDIT_FORM and DELETE_FORM permissions on root database
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.CREATE_FORM,
                db.getDatabaseId());
        Permission createForm = PermissionOracle.query(query, db);

        query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.EDIT_FORM,
                db.getDatabaseId());
        Permission editForm = PermissionOracle.query(query, db);

        query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.DELETE_FORM,
                db.getDatabaseId());
        Permission deleteForm = PermissionOracle.query(query, db);

        return createForm.isPermitted() && editForm.isPermitted() && deleteForm.isPermitted();
    }

    public static boolean canManagePartner(ResourceId resourceId, int partnerId, UserDatabaseMeta db) {
        Permission managePartner = manageUsers(resourceId, db);
        if (managePartner.isForbidden()) {
            return false;
        }
        if (!managePartner.isFiltered()) {
            return true;
        }
        return filterContainsPartner(managePartner.getFilter(),
                CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                CuidAdapter.partnerRecordId(partnerId));
    }

    public static boolean canManageAllPartners(ResourceId resourceId, UserDatabaseMeta db) {
        Permission managePartner = manageUsers(resourceId, db);
        if (managePartner.isForbidden()) {
            return false;
        }
        return !managePartner.isFiltered();
    }

}