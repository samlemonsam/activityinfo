package org.activityinfo.server.database.hibernate;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.formula.ConstantNode;
import org.activityinfo.model.formula.FunctionCallNode;
import org.activityinfo.model.formula.SymbolNode;
import org.activityinfo.model.formula.functions.EqualFunction;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.activityinfo.store.spi.DatabaseGrantProvider;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HibernateDatabaseGrantProvider implements DatabaseGrantProvider {

    private static final Logger LOGGER = Logger.getLogger(HibernateDatabaseGrantProvider.class.getName());

    private final Provider<EntityManager> entityManager;

    @Inject
    public HibernateDatabaseGrantProvider(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public @Nullable DatabaseGrant getDatabaseGrant(int userId, @NotNull ResourceId databaseId) {
        try {
            UserPermission userPermission = entityManager.get().createQuery("SELECT up " +
                    "FROM UserPermission up " +
                    "JOIN FETCH up.database " +
                    "JOIN FETCH up.user " +
                    "WHERE up.user.id=:userId " +
                    "AND up.database.id=:databaseId", UserPermission.class)
                    .setParameter("userId", userId)
                    .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                    .getSingleResult();
            return buildDatabaseGrant(userPermission);
        } catch (NoResultException noGrant) {
            return null;
        }
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForUser(int userId) {
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "JOIN FETCH up.database " +
                "JOIN FETCH up.user " +
                "WHERE up.user.id = :userId", UserPermission.class)
                .setParameter("userId", userId)
                .getResultList().stream()
                .map(HibernateDatabaseGrantProvider::buildDatabaseGrant)
                .collect(Collectors.toList());
    }

    @Override
    public List<DatabaseGrant> getAllDatabaseGrantsForDatabase(@NotNull ResourceId databaseId) {
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "JOIN FETCH up.database " +
                "JOIN FETCH up.user " +
                "WHERE up.database.id = :databaseId", UserPermission.class)
                .setParameter("databaseId", CuidAdapter.getLegacyIdFromCuid(databaseId))
                .getResultList().stream()
                .map(HibernateDatabaseGrantProvider::buildDatabaseGrant)
                .collect(Collectors.toList());
    }

    private static DatabaseGrant buildDatabaseGrant(UserPermission userPermission) {
        return new DatabaseGrant.Builder()
                .setUserId(userPermission.getUser().getId())
                .setDatabaseId(CuidAdapter.databaseId(userPermission.getDatabase().getId()))
                .setVersion(userPermission.getVersion())
                .addGrants(buildGrants(userPermission))
                .build();
    }

    private static List<GrantModel> buildGrants(UserPermission userPermission) {
        List<GrantModel> grants = new ArrayList<>();
        if(!userPermission.isAllowView()) {
            return grants;
        }
        grants.add(buildRootGrant(CuidAdapter.databaseId(userPermission.getDatabase().getId()), userPermission));
        if (userPermission.getModel() == null) {
            return grants;
        }
        JsonValue modelObject = Json.parse(userPermission.getModel());
        grants.addAll(buildGrantsFromModel(modelObject));
        return grants;
    }

    private static GrantModel buildRootGrant(ResourceId databaseId, UserPermission userPermission) {
        GrantModel.Builder databaseGrant = new GrantModel.Builder();
        databaseGrant.setResourceId(databaseId);
        setOperations(databaseGrant, userPermission);
        return databaseGrant.build();
    }

    private static List<GrantModel> buildGrantsFromModel(JsonValue modelObject) {
        if (!modelObject.hasKey("grants")) {
            LOGGER.severe(() -> "Could not parse permissions model: " + modelObject);
            throw new UnsupportedOperationException("Unsupported model");
        }
        List<GrantModel> grants = new ArrayList<>();
        for (JsonValue grant : modelObject.get("grants").values()) {
            GrantModel grantModel = GrantModel.fromJson(grant);
            grants.add(grantModel);
        }
        return grants;
    }

    private static void setOperations(GrantModel.Builder grantModel, UserPermission userPermission) {
        if(userPermission.isAllowViewAll()) {
            grantModel.addOperation(Operation.VIEW);
        } else if(userPermission.isAllowView()) {
            grantModel.addOperation(Operation.VIEW, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowEditAll()) {
            grantModel.addOperation(Operation.EDIT_RECORD);
            grantModel.addOperation(Operation.CREATE_RECORD);
            grantModel.addOperation(Operation.DELETE_RECORD);
        } else if(userPermission.isAllowEdit()) {
            grantModel.addOperation(Operation.EDIT_RECORD, getPartnerFilter(userPermission));
            grantModel.addOperation(Operation.CREATE_RECORD, getPartnerFilter(userPermission));
            grantModel.addOperation(Operation.DELETE_RECORD, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowManageAllUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS);
        } else if(userPermission.isAllowManageUsers()) {
            grantModel.addOperation(Operation.MANAGE_USERS, getPartnerFilter(userPermission));
        }
        if(userPermission.isAllowDesign()) {
            grantModel.addOperation(Operation.CREATE_FORM);
            grantModel.addOperation(Operation.EDIT_FORM);
            grantModel.addOperation(Operation.DELETE_FORM);
            grantModel.addOperation(Operation.LOCK_RECORDS);
            grantModel.addOperation(Operation.MANAGE_TARGETS);
        }
    }

    private static String getPartnerFilter(UserPermission userPermission) {
        SymbolNode partnerForm = new SymbolNode(CuidAdapter.partnerFormId(userPermission.getDatabase().getId()));
        ConstantNode partnerRecord = new ConstantNode(CuidAdapter.partnerRecordId(userPermission.getPartner().getId()).asString());
        return new FunctionCallNode(EqualFunction.INSTANCE, partnerForm, partnerRecord).asExpression();
    }

}
