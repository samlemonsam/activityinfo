package org.activityinfo.model.permission;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activityinfo.model.database.Operation;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.formula.FormulaParser;
import org.activityinfo.model.formula.Formulas;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

public class PermissionOracle {

    public static Permission query(PermissionQuery query, UserDatabaseMeta db) {
        if (db.isOwner()) {
            return allowOwner(query.getOperation());
        }
        if (!db.isVisible()) {
            return deny(query.getOperation());
        }
        if (!isDatabase(query.getResourceId()) && !db.hasResource(query.getResourceId())) {
            return deny(query.getOperation());
        }
        return determinePermission(query.getOperation(), query.getResourceId(), db);
    }

    /**
     * Allow the owner of a database full permissions with no record filters
     */
    private static Permission allowOwner(Operation operation) {
        return new Permission(operation, true, Optional.absent());
    }

    /**
     * Deny permission outright for the specified operation
     */
    private static Permission deny(Operation operation) {
        return new Permission(operation, false, Optional.absent());
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
            return db.getDatabaseId().equals(resourceId) && db.getGrant(resourceId).hasOperation(operation);
        } else {
            return db.hasResource(resourceId) && granted(operation, db.getResource(resourceId), db);
        }
    }

    /**
     * Checks whether the specified {@link Operation} has been granted on the given {@link Resource}, or on the
     * <b>closest</b> parent resource with an explicit grant
     */
    private static boolean granted(Operation operation, Resource resource, UserDatabaseMeta db) {
        // If there is an explicit grant, check whether the operation is granted at this level
        if (db.hasGrant(resource.getId())) {
            return db.getGrant(resource.getId()).hasOperation(operation);
        }
        // As there is no grant defined at this level, we need to check further up the Resource tree
        // If the parent of this resource is the root database, then check whether operation exists on database grant
        if (isDatabase(resource.getParentId())) {
            return db.getGrant(resource.getParentId()).hasOperation(operation);
        }
        // Otherwise, we climb the resource tree to determine whether the operation is granted there
        return granted(operation, db.getResource(resource.getParentId()), db);
    }

    private static Optional<String> operationFilter(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        if (isDatabase(resourceId)) {
            return getFilter(operation, resourceId, db);
        } else {
            return collectFilters(operation, db.getResource(resourceId), db);
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
        return and(filter, collectFilters(operation, db.getResource(resource.getParentId()), db));
    }

    private static Optional<String> getFilter(Operation operation, ResourceId resourceId, UserDatabaseMeta db) {
        Optional<String> filter = Optional.absent();
        if (db.hasGrant(resourceId) && db.getGrant(resourceId).hasOperation(operation)) {
            filter = db.getGrant(resourceId).getFilter(operation);
        }
        return filter;
    }

    private static Optional<String> and(Optional<String> filter1, Optional<String> filter2) {
        if (!filter1.isPresent() && !filter2.isPresent()) {
            return Optional.absent();
        } else if (!filter1.isPresent()) {
            return filter2;
        } else if (!filter2.isPresent()) {
            return filter1;
        } else {
            FormulaNode filterFormula1 = FormulaParser.parse(filter1.get());
            FormulaNode filterFormula2 = FormulaParser.parse(filter2.get());
            FormulaNode and = Formulas.allTrue(Lists.newArrayList(filterFormula1, filterFormula2));
            return Optional.of(and.asExpression());
        }
    }

    private static boolean isDatabase(ResourceId resourceId) {
        return resourceId.getDomain() == CuidAdapter.DATABASE_DOMAIN;
    }

    ////////////////////////////////////////////////// TASK METHODS //////////////////////////////////////////////////

    public static boolean canDeleteDatabase(UserDatabaseMeta db) {
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

}
