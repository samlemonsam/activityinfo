package org.activityinfo.model.permission;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.model.database.RecordLockSet;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.formula.*;
import org.activityinfo.model.formula.diagnostic.FormulaException;
import org.activityinfo.model.formula.eval.EvalContext;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.BooleanFieldValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionOracle {

    private PermissionOracle() {
    }

    public static Permission query(PermissionQuery query, UserDatabaseMeta db) {
        if (db.isDeleted()) {
            return deny(query.getOperation());
        }
        if (db.isOwner()) {
            return allowOwner(query.getOperation());
        }
        if (db.isPublished()) {
            switch (query.getOperation()) {
                case VIEW:
                    return allow(query.getOperation());

                case CREATE_RECORD:
                case EDIT_RECORD:
                case DELETE_RECORD:
                    // Carve out special permissions for public location types.
                    if(query.getResourceId().getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN) {
                        return allow(query.getOperation());
                    }
                    break;
            }
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
        } else if (isProjectForm(resourceId)) {
            return allowedProjectOperation(resourceId, operation, db);
        } else if (isAdminLevelForm(resourceId)) {
            return allowedAdminLevelOperation(operation, db);
        } else {
            return db.hasResource(resourceId)
                    && granted(operation, db.getResource(resourceId).get(), db);
        }
    }

    private static boolean allowedProjectOperation(ResourceId projectFormId, Operation operation, UserDatabaseMeta db) {
        switch(operation) {
            case VIEW:
                return db.isVisible();
            case CREATE_RECORD:
            case EDIT_RECORD:
            case DELETE_RECORD:
            case EXPORT_RECORDS:
                return canEditResource(projectFormId, db);
            default:
                return false;
        }
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
        // 1. Check for VIEW operation requests on Resources which are Public or Visible as References
        if (Operation.VIEW.equals(operation) && (resource.isPublic() || resource.isReference())) {
            return true;
        }
        // 2. Check for VIEW operation requests on Resources which are Public to Database Users (must have grants present)
        if (Operation.VIEW.equals(operation) && resource.isPublicToDatabaseUsers() && !db.getGrants().isEmpty()) {
            return true;
        }
        // 3. Check further up the Resource tree:
        // -> If the parent of this resource is the root database, then check whether operation exists on database grant
        if (isDatabase(resource.getParentId())) {
            return db.hasGrant(resource.getParentId())
                    && db.getGrant(resource.getParentId()).get().hasOperation(operation);
        }
        // -> If we are already at the end of the resource tree defined on database, then we have no grant for this operation
        if (!db.hasResource(resource.getParentId())) {
            return false;
        }
        // -> Otherwise, we climb the resource tree to determine whether the operation is granted there
        return granted(operation, db.getResource(resource.getParentId()).get(), db);
    }

    private static Optional<String> operationFilter(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        if (isDatabase(resourceId)) {
            // If this is a database request, get filter on the root grant (if any)
            return getFilter(operation, resourceId, db);
        } else if (isProjectForm(resourceId)) {
            // Project forms have no filter
            return Optional.empty();
        } else if (isAdminLevelForm(resourceId)) {
            // Admin Level forms have no filter
            return Optional.empty();
        } else {
            // Otherwise collect filters on each level of resource tree
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
        if (!db.hasResource(resource.getParentId())) {
            return filter;
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

    private static boolean isProjectForm(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.PROJECT_CLASS_DOMAIN;
    }

    private static boolean isAdminLevelForm(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN;
    }

    ///////////////////////////////////////////// FORM PERMISSION METHODS //////////////////////////////////////////////

    public static FormPermissions formPermissions(ResourceId formId, UserDatabaseMeta db) {
        if (!db.isVisible() || db.isDeleted()) {
            return FormPermissions.none();
        }
        if (db.isOwner()) {
            return FormPermissions.owner(db.getEffectiveLocks(formId));
        }
        if (db.isPublished()) {
            if(formId.getDomain() == CuidAdapter.LOCATION_TYPE_DOMAIN) {
                return FormPermissions.readWrite(db.getEffectiveLocks(formId));
            } else {
                return FormPermissions.readonly();
            }
        }
        if (isProjectForm(formId)) {
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
        computeExportRecordsFormPermissions(permissionsBuilder, formId, db);
        computeLocks(permissionsBuilder, formId, db);
        return permissionsBuilder.build();
    }

    private static void computeLocks(FormPermissions.Builder permissionsBuilder, ResourceId formId, UserDatabaseMeta db) {
        RecordLockSet formLocks = db.getEffectiveLocks(formId);
        if (!formLocks.isEmpty()) {
            permissionsBuilder.lock(formLocks);
        }
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
        Permission createRecord = createRecord(formId, db);
        Permission editRecord = editRecord(formId, db);
        Permission deleteRecord = deleteRecord(formId, db);

        if (createRecord.isForbidden() && editRecord.isForbidden() && deleteRecord.isForbidden()) {
            return;
        }

        if (createRecord.isPermitted()) {
            builder.allowCreate(createRecord.getOptionalFilter());
        }
        if (editRecord.isPermitted()) {
            builder.allowEdit(editRecord.getOptionalFilter());
        }
        if (deleteRecord.isPermitted()) {
            builder.allowDelete(deleteRecord.getOptionalFilter());
        }
    }

    private static void computeExportRecordsFormPermissions(FormPermissions.Builder builder,
                                                            ResourceId formId,
                                                            UserDatabaseMeta db) {
        if (canExportRecords(formId, db)) {
            builder.allowExport();
        }
    }

    /////////////////////////////////////////////// FORM INSTANCE METHODS ////////////////////////////////////////////////

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

    public static boolean canCreate(TypedFormRecord record,
                                  FormPermissions formPermissions,
                                  FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.CREATE_RECORD);
    }

    public static boolean canEdit(TypedFormRecord record,
                                  FormPermissions formPermissions,
                                  FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.EDIT_RECORD);
    }

    public static boolean canDelete(TypedFormRecord record,
                                    FormPermissions formPermissions,
                                    FormClass formClass) {
        return can(record, formPermissions, formClass, Operation.DELETE_RECORD);
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

    private static boolean filterAllowsPartner(Permission permission, int partnerId, UserDatabaseMeta db) {
        return !permission.isFiltered()
                || filterContainsPartner(permission.getFilter(),
                    CuidAdapter.partnerFormId(db.getLegacyDatabaseId()),
                    CuidAdapter.partnerRecordId(partnerId));
    }

    private static boolean filterContainsPartner(String filter, ResourceId partnerFormId, ResourceId partnerId) {
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

    ///////////////////////////////////////////// BASIC PERMISSION QUERIES /////////////////////////////////////////////

    public static Permission view(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.VIEW,
                resourceId);
        return query(query, db);
    }

    public static Permission createRecord(ResourceId formId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.CREATE_RECORD,
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

    public static Permission editRecord(ResourceId formId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.EDIT_RECORD,
                formId);
        return query(query, db);
    }

    public static Permission manageUsers(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.MANAGE_USERS,
                resourceId);
        return query(query, db);
    }

    public static Permission createResource(ResourceId containerResourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.CREATE_RESOURCE,
                containerResourceId);
        return query(query, db);
    }

    public static Permission deleteResource(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.DELETE_RESOURCE,
                resourceId);
        return query(query, db);
    }

    public static Permission editResource(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.EDIT_RESOURCE,
                resourceId);
        return query(query, db);
    }

    public static Permission lockRecords(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.LOCK_RECORDS,
                resourceId);
        return query(query, db);
    }

    public static Permission exportRecords(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.EXPORT_RECORDS,
                resourceId);
        return query(query, db);
    }

    public static Permission manageTargets(ResourceId resourceId, UserDatabaseMeta db) {
        PermissionQuery query = new PermissionQuery(db.getUserId(),
                db.getLegacyDatabaseId(),
                Operation.MANAGE_TARGETS,
                resourceId);
        return query(query, db);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////// TASK METHODS ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////// DATABASE Methods /////////////////////////////////////////////////

    /**
     * <p>The current User can transfer this Database.</p>
     */
    public static boolean canTransferDatabase(UserDatabaseMeta db) {
        return db.isOwner();
    }

    /**
     * <p>The current User can delete this Database.</p>
     */
    public static boolean canDeleteDatabase(UserDatabaseMeta db) {
        return db.isOwner();
    }

    /**
     * <p>The current User can edit this Database's metadata.</p>
     */
    public static boolean canEditDatabase(UserDatabaseMeta db) {
        return db.isOwner();
    }

    /////////////////////////////////////////////////// VIEW Methods ///////////////////////////////////////////////////

    /**
     * <p>The current User can view the root Resource(s) in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canView(UserDatabaseMeta db) {
        return view(db.getDatabaseId(),db).isPermitted();
    }

    /**
     * <p>The current User can view the specified Resource in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canView(ResourceId resourceId, UserDatabaseMeta db) {
        return view(resourceId,db).isPermitted();
    }

    /**
     * <p>The current User can view a Site, with given Partner Id, on the specified Activity in this Database.</p>
     * <p>Explicitly checks the filter to ensure that User is permitted to view Sites <b>for this Partner.</b></p>
     */
    public static boolean canViewSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission view = view(activityId, db);
        return view.isPermitted() && filterAllowsPartner(view, partnerId, db);
    }

    ////////////////////////////////////////////// CREATE_RECORD Methods ///////////////////////////////////////////////

    /**
     * <p>The current User can create a Record within the specified Form in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canCreateRecord(ResourceId formId, UserDatabaseMeta db) {
        return createRecord(formId,db).isPermitted();
    }

    /**
     * <p>The current User can create a Site, with given Partner Id, on the specified Activity in this Database.</p>
     * <p>Explicitly checks the filter to ensure that User is permitted to create Sites <b>for this Partner.</b></p>
     */
    public static boolean canCreateSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission create = createRecord(activityId, db);
        return create.isPermitted() && filterAllowsPartner(create, partnerId, db);
    }

    ////////////////////////////////////////////// DELETE_RECORD Methods ///////////////////////////////////////////////

    /**
     * <p>The current User can delete a Record within the specified Form in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canDeleteRecord(ResourceId formId, UserDatabaseMeta db) {
        return deleteRecord(formId,db).isPermitted();
    }

    /**
     * <p>The current User can delete a Site, with given Partner Id, on the specified Activity in this Database.</p>
     * <p>Explicitly checks the filter to ensure that User is permitted to delete Sites <b>for this Partner.</b></p>
     */
    public static boolean canDeleteSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission delete = deleteRecord(activityId, db);
        return delete.isPermitted() && filterAllowsPartner(delete, partnerId, db);
    }

    /////////////////////////////////////////////// EDIT_RECORD Methods ////////////////////////////////////////////////

    /**
     * <p>The current User can edit a Record within the specified Form in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canEditRecord(ResourceId formId, UserDatabaseMeta db) {
        return editRecord(formId,db).isPermitted();
    }

    /**
     * <p>The current User can edit a Site, with given Partner Id, on the specified Activity in this Database.</p>
     * <p>Explicitly checks the filter to ensure that User is permitted to edit Sites <b>for this Partner.</b></p>
     */
    public static boolean canEditSite(ResourceId activityId, int partnerId, UserDatabaseMeta db) {
        Permission edit = editRecord(activityId, db);
        return edit.isPermitted() && filterAllowsPartner(edit, partnerId, db);
    }

    ///////////////////////////////////////////// CREATE_RESOURCE Methods //////////////////////////////////////////////

    /**
     * <p>The current User can create a new Resource <i>within</i> the specified Resource in this Database.</p>
     */
    public static boolean canCreateResource(ResourceId containerResourceId, UserDatabaseMeta db) {
        return createResource(containerResourceId,db).isPermitted();
    }

    /**
     * <p>The current User can create a new Form <i>within</i> the specified Resource in this Database.</p>
     */
    public static boolean canCreateForm(ResourceId containerResourceId, UserDatabaseMeta db) {
        return canCreateResource(containerResourceId, db);
    }

    ///////////////////////////////////////////// DELETE_RESOURCE Methods //////////////////////////////////////////////

    /**
     * <p>The current User can delete the specified Resource in this Database.</p>
     */
    public static boolean canDeleteResource(ResourceId resourceId, UserDatabaseMeta db) {
        return deleteResource(resourceId,db).isPermitted();
    }

    /**
     * <p>The current User can delete the specified Form in this Database.</p>
     */
    public static boolean canDeleteForm(ResourceId formId, UserDatabaseMeta db) {
        return canDeleteResource(formId, db);
    }

    /**
     * <p>The current User can delete the specified Folder in this Database.</p>
     */
    public static boolean canDeleteFolder(ResourceId folderId, UserDatabaseMeta db) {
        return canDeleteResource(folderId, db);
    }

    ////////////////////////////////////////////// EDIT_RESOURCE Methods ///////////////////////////////////////////////

    /**
     * <p>The current User can edit the specified Resource in this Database.</p>
     */
    public static boolean canEditResource(ResourceId resourceId, UserDatabaseMeta db) {
        return editResource(resourceId,db).isPermitted();
    }

    /**
     * <p>The current User can edit the specified Form in this Database.</p>
     */
    public static boolean canEditForm(ResourceId formId, UserDatabaseMeta db) {
        return canEditResource(formId, db);
    }

    /**
     * <p>The current User can edit the specified Folder in this Database.</p>
     */
    public static boolean canEditFolder(ResourceId folderId, UserDatabaseMeta db) {
        return canEditResource(folderId, db);
    }

    ////////////////////////////////////////////// LOCK_RECORDS Methods ////////////////////////////////////////////////

    /**
     * <p>The current User can lock records on the specified Resource (and all children) in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canLockRecords(ResourceId resourceId, UserDatabaseMeta db) {
        return lockRecords(resourceId,db).isPermitted();
    }

    ///////////////////////////////////////////// EXPORT_RECORDS Methods ///////////////////////////////////////////////

    /**
     * <p>The current User can export records from the specified Resource in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canExportRecords(ResourceId resourceId, UserDatabaseMeta db) {
        return exportRecords(resourceId, db).isPermitted();
    }

    ///////////////////////////////////////////// MANAGE_TARGETS Methods ///////////////////////////////////////////////

    /**
     * <p>The current User can manage targets on the specified Resource in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canManageTargets(ResourceId resourceId, UserDatabaseMeta db) {
        return manageTargets(resourceId,db).isPermitted();
    }

    ////////////////////////////////////////////// MANAGE_USERS Methods ////////////////////////////////////////////////

    /**
     * <p>The current User can manage users for any Resource on this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canManageUsersOnWholeDatabase(UserDatabaseMeta db) {
        return manageUsers(db.getDatabaseId(),db).isPermitted();
    }

    /**
     * <p>The current User can manage users on the specified Resource in this Database.</p>
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canManageUsersOnResource(ResourceId resourceId, UserDatabaseMeta db) {
        return manageUsers(resourceId,db).isPermitted();
    }

    /**
     * <p>The current User can manage users, with given Partner Id, on the specified Resource in this Database.</p>
     * <p>Explicitly checks the filter to ensure that User is permitted to manage users <b>for this Partner.</b></p>
     */
    public static boolean canManagePartner(ResourceId resourceId, int partnerId, UserDatabaseMeta db) {
        Permission manageUsers = manageUsers(resourceId, db);
        return manageUsers.isPermitted() && filterAllowsPartner(manageUsers, partnerId, db);
    }

    /**
     * <p>The current User can manage users, with <i>any</i> Partner Id, on the specified Resource in this Database.</p>
     * <p>Explicitly checks to ensure User has <b>no filters</b> associated with this Permission.</p>
     */
    public static boolean canManageAllPartners(ResourceId resourceId, UserDatabaseMeta db) {
        Permission manageUsers = manageUsers(resourceId, db);
        return manageUsers.isPermitted() && !manageUsers.isFiltered();
    }

    /**
     * <p>The current User can manage users for <b>one or more</b> Resources. If the User is <b>not</b> permitted to
     * manage users on <i>any</i> Resources on the database, then the User cannot manage users at all and this method
     * will return false.</p>
     *
     * <p>Does not account for any filters which may be applied/enforced.</p>
     */
    public static boolean canManageUsersForOneOrMoreResources(UserDatabaseMeta db) {
        // If we have a root database grant, check if we can grant users at this level
        if (canManageUsersOnWholeDatabase(db)) {
            return true;
        }
        // If not, check if we have any resources for which we can manage users
        return !resourcesWithManageUserRights(db).isEmpty();
    }

    private static List<Resource> resourcesWithManageUserRights(UserDatabaseMeta db) {
        return db.getResources().stream()
                .filter(r -> canManageUsersOnResource(r.getId(), db))
                .collect(Collectors.toList());
    }

    /**
     * <p>The legacy permission model restricts a Users ability to manage users to All Partners or a specified
     * Partner, across the set of Resources which the User has access to.</p>
     *
     * <p>The Partner restriction is consistent across all "manageable" Resources. Therefore, we find the first filter
     * defined on a manageable resource as it will be the same for all other manageable resources.</p>
     */
    public static Optional<String> legacyManageUserFilter(UserDatabaseMeta db) {
        // If we have a root database grant, then return the filter defined there
        if (canManageUsersOnWholeDatabase(db)) {
            return manageUsers(db.getDatabaseId(),db).getOptionalFilter();
        }
        // If not, then check for the first filter we find on a manageable resource
        return db.getResources().stream()
                .map(r -> manageUsers(r.getId(), db))
                .filter(Permission::isPermitted)
                .map(Permission::getOptionalFilter)
                .findFirst().orElse(Optional.empty());
    }

}