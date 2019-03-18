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
package org.activityinfo.server.digest.activity;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.Partner;
import org.activityinfo.server.database.hibernate.entity.SiteHistory;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.digest.DigestModelBuilder;
import org.activityinfo.server.digest.UserDigest;
import org.activityinfo.server.digest.activity.ActivityDigestModel.ActivityMap;
import org.activityinfo.server.digest.activity.ActivityDigestModel.DatabaseModel;
import org.activityinfo.server.digest.activity.ActivityDigestModel.PartnerActivityModel;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ActivityDigestModelBuilder implements DigestModelBuilder {

    private static final Logger LOGGER = Logger.getLogger(ActivityDigestModelBuilder.class.getName());

    private final Provider<EntityManager> entityManager;

    @Inject
    public ActivityDigestModelBuilder(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ActivityDigestModel createModel(UserDigest userDigest) throws IOException {

        ActivityDigestModel model = new ActivityDigestModel(userDigest);

        List<Database> databases = findDatabases(userDigest.getUser());
        LOGGER.finest("found " + databases.size() + " database(s) for user " + userDigest.getUser().getId());

        if (!databases.isEmpty()) {
            for (Database database : databases) {
                createDatabaseModel(model, database);
            }
        }

        return model;
    }

    private void createDatabaseModel(ActivityDigestModel model, Database database) {

        SiteHistory lastEdit = findLastEdit(database);
        // only include databases that are known to be edited at least once
        if (lastEdit != null) {
            DatabaseModel databaseModel = new DatabaseModel(model, database, lastEdit);

            List<SiteHistory> ownerHistories = findSiteHistory(databaseModel, database.getOwner());
            ActivityMap ownerActivityMap = new ActivityMap(databaseModel, database.getOwner(), ownerHistories);
            databaseModel.setOwnerActivityMap(ownerActivityMap);

            List<Partner> partners = findPartners(databaseModel);
            LOGGER.finest("building user activity digest for user " + model.getUserDigest().getUser().getId() +
                          " and database " + database.getId() + " - found " + partners.size() + " partner(s)");
            if (!partners.isEmpty()) {
                for (Partner partner : partners) {
                    PartnerActivityModel partnerModel = new PartnerActivityModel(databaseModel, partner);

                    List<User> partnerUsers = findUsers(partnerModel);
                    LOGGER.finest("found users " + partnerUsers + " for partner " + partner.getName());
                    if (!partnerUsers.isEmpty()) {
                        for (User partnerUser : partnerUsers) {
                            List<SiteHistory> histories = findSiteHistory(databaseModel, partnerUser);
                            ActivityMap activityMap = new ActivityMap(databaseModel, partnerUser, histories);
                            partnerModel.addActivityMap(activityMap);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param user
     * @return all UserDatabases for the specified user where the user is the database owner, or where the database has
     * a UserPermission for the specified user with allowDesign set to true. If the user happens to have his
     * emailnotification preference set to false, an empty list is returned.
     */
    @VisibleForTesting @SuppressWarnings("unchecked") List<Database> findDatabases(User user) {
        // sanity check
        if (!user.isEmailNotification()) {
            return new ArrayList<>();
        }

        Query query = entityManager.get()
                                   .createQuery("select distinct d from Database d left join d.userPermissions p " +
                                                "where (d.owner = :user or (p.user = :user and p.allowDesign = true))" +
                                                " and d.dateDeleted is null " +
                                                "order by d.name");
        query.setParameter("user", user);

        return query.getResultList();
    }

    /**
     * @param databaseModel
     * @return the partners linked to the specified database via a userpermission
     */
    @VisibleForTesting @SuppressWarnings("unchecked") List<Partner> findPartners(DatabaseModel databaseModel) {

        Query query = entityManager.get().createQuery("select distinct p from Partner p " +
                                                        "join p.userPermissions up " +
                                                        "where up.database = :database " +
                                                        "order by p.name");
        query.setParameter("database", databaseModel.getDatabase());


        return query.getResultList();
    }

    /**
     * @param partnerModel
     * @return the users linked to the specified database and partner via a userpermission where allowEdit is set to
     * true.
     */
    @VisibleForTesting @SuppressWarnings("unchecked") List<User> findUsers(PartnerActivityModel partnerModel) {

        Query query = entityManager.get().createQuery("select distinct p.user from UserPermission p " +
                                                        "where p.database = :database " +
                                                        "and :partner member of p.partners " +
                                                        "and p.allowEdit = true " +
                                                        "order by p.user.name");
        query.setParameter("database", partnerModel.getDatabaseModel().getDatabase());
        query.setParameter("partner", partnerModel.getPartner());

        return query.getResultList();
    }

    /**
     * @param databaseModel
     * @param editor
     * @return the sitehistory edited since the specified timestamp (milliseconds) and linked to the specified database
     * and user.
     */
    @VisibleForTesting @SuppressWarnings("unchecked")
    List<SiteHistory> findSiteHistory(DatabaseModel databaseModel, User editor) {

        Query query = entityManager.get().createQuery("select distinct h from SiteHistory h " +
                                                      "where h.site.activity.database = :database and h.user = :user " +
                                                      "and h.timeCreated >= :from " +
                                                      "order by h.timeCreated");
        query.setParameter("database", databaseModel.getDatabase());
        query.setParameter("user", editor);
        query.setParameter("from", databaseModel.getModel().getUserDigest().getFrom());

        return query.getResultList();
    }

    @VisibleForTesting @SuppressWarnings("unchecked") SiteHistory findLastEdit(Database database) {

        Query query = entityManager.get().createQuery("select h from SiteHistory h " +
                                                      "where h.site.activity.database = :database " +
                                                      "order by h.timeCreated desc");
        query.setParameter("database", database);
        query.setMaxResults(1);

        List<SiteHistory> list = query.getResultList();
        return (list != null && list.size() == 1) ? list.get(0) : null;
    }
}
