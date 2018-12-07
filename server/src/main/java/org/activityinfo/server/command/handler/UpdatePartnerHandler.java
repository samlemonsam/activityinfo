/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.UpdatePartner;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.DuplicateCreateResult;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.spi.DatabaseProvider;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 * @see UpdatePartner
 */
public class UpdatePartnerHandler implements CommandHandler<UpdatePartner> {

    private static final Logger LOGGER = Logger.getLogger(UpdatePartnerHandler.class.getName());

    private final EntityManager em;
    private final DatabaseProvider provider;

    @Inject
    public UpdatePartnerHandler(EntityManager em, DatabaseProvider provider) {
        this.em = em;
        this.provider = provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult execute(UpdatePartner cmd, User user) {

        Database db = em.find(Database.class, cmd.getDatabaseId());
        Optional<UserDatabaseMeta> dbMeta = provider.getDatabaseMetadata(cmd.getDatabaseId(), user.getId());

        assert db != null && dbMeta.isPresent();

        // Does this partner already exist?
        if (cmd.getPartner().hasId()) {
            assertManagePartnerAllowed(dbMeta.get(), cmd.getPartner().getId());
            return updatePartner(db, cmd);
        } else {
            assertManageAllPartnersAllowed(dbMeta.get());
            return addNewPartner(cmd, db);
        }
    }

    private void assertManagePartnerAllowed(UserDatabaseMeta dbMeta, int partnerId) {
        if (!PermissionOracle.canManagePartner(dbMeta.getDatabaseId(), partnerId, dbMeta)) {
            LOGGER.severe(String.format("User %d is not authorized to modify partner %d", dbMeta.getUserId(), partnerId));
            throw new IllegalAccessCommandException();
        }
    }

    private void assertManageAllPartnersAllowed(UserDatabaseMeta dbMeta) {
        if (!PermissionOracle.canManageAllPartners(dbMeta.getDatabaseId(), dbMeta)) {
            LOGGER.severe(String.format("User %d is not authorized to modify all partners", dbMeta.getUserId()));
            throw new IllegalAccessCommandException();
        }
    }

    private CreateResult updatePartner(Database db, UpdatePartner cmd) {
        Partner partner = em.find(Partner.class, cmd.getPartner().getId());
        if (partner == null) {
            LOGGER.severe("Partner " + cmd.getPartner() + " is not ");
            throw new IllegalArgumentException("No such partner");
        }

        boolean ownedByThisDatabase = false;
        boolean shared = false;

        LOGGER.info("Partner is shared between " + partner.getDatabases().size() + " database(s)");

        for (Database database : partner.getDatabases()) {
            if (database.getId() == cmd.getDatabaseId()) {
                ownedByThisDatabase = true;
            } else {
                shared = true;
            }
        }

        if (!ownedByThisDatabase) {
            LOGGER.info("Partner " + cmd.getPartner() + " is not associated with database " + cmd.getDatabaseId());
            throw new IllegalArgumentException("Partner not owned by the database");
        }

        // Are there any changes to actually make?
        if (Objects.equals(partner.getName(), cmd.getPartner().getName()) &&
                Objects.equals(partner.getFullName(), cmd.getPartner().getFullName())) {
            LOGGER.info("No changes to make, stopping.");
            return new CreateResult(cmd.getPartner().getId());
        }

        // Increment the database's version number
        db.setLastSchemaUpdate(new Date());


        // If this partner is shared, then duplicate and update all references.
        if (shared) {
            return copyAndReplace(partner, cmd);
        } else {
            return simpleUpdate(partner, cmd);
        }
    }

    /**
     * Originally, partner objects were shared between databases, which meant that it was not possible
     * for a single database owner to change the partner's name or description.
     * <p>
     * <p>To move forward, we apply a "copy-on-write" strategy. If a user wants to change a partner, and it
     * is shared with another database, then create a new copy of the partner with the desired changes,
     * and update all references in this database.</p>
     */
    private CreateResult copyAndReplace(Partner sharedPartner, UpdatePartner cmd) {

        LOGGER.info("Copying and replacing " + sharedPartner + " with " + cmd.getPartner());

        Partner newPartner = new Partner();
        newPartner.setName(cmd.getPartner().getName());
        newPartner.setFullName(cmd.getPartner().getFullName());
        em.persist(newPartner);

        assert newPartner.getId() != 0;

        em.createNativeQuery("UPDATE site SET partnerId = ? WHERE partnerId = ? AND activityId IN " +
                                   "(SELECT activityid FROM activity WHERE databaseId = ?)")
                .setParameter(1, newPartner.getId())
                .setParameter(2, sharedPartner.getId())
                .setParameter(3, cmd.getDatabaseId())
                .executeUpdate();

        em.createNativeQuery("UPDATE activity SET version = version+1, siteVersion = siteVersion+1, schemaVersion=schemaVersion+1 WHERE databaseId = ? ")
                .setParameter(1, cmd.getDatabaseId())
                .executeUpdate();

        em.createNativeQuery("UPDATE userpermission SET partnerId = ? where partnerId = ? and databaseId = ?")
                .setParameter(1, newPartner.getId())
                .setParameter(2, sharedPartner.getId())
                .setParameter(3, cmd.getDatabaseId())
                .executeUpdate();

        em.createNativeQuery("UPDATE partnerindatabase SET partnerId = ? WHERE databaseId = ? AND partnerId = ?")
                .setParameter(1, newPartner.getId())
                .setParameter(2, cmd.getDatabaseId())
                .setParameter(3, sharedPartner.getId())
                .executeUpdate();


        return new CreateResult(newPartner.getId());
    }


    /**
     * This partner is not or is no longer shared between databases, so we can just update the row
     */
    private CreateResult simpleUpdate(Partner partner, UpdatePartner cmd) {
        partner.setName(cmd.getPartner().getName());
        partner.setFullName(cmd.getPartner().getFullName());

        return new CreateResult(cmd.getPartner().getId());
    }


    private CreateResult addNewPartner(UpdatePartner cmd, Database db) {
        // first check to see if an organization by this name is already
        // a partner in the same database

        Set<Partner> dbPartners = db.getPartners();
        for (Partner partner : dbPartners) {
            if (partner.getName().equals(cmd.getPartner().getName())) {
                return new DuplicateCreateResult();
            }
        }

        // Add a new partner

        Partner partner = new Partner();
        partner.setName(cmd.getPartner().getName());
        partner.setFullName(cmd.getPartner().getFullName());
        em.persist(partner);

        db.setLastSchemaUpdate(new Date());
        em.persist(db);

        db.getPartners().add(partner);

        return new CreateResult(partner.getId());
    }

}