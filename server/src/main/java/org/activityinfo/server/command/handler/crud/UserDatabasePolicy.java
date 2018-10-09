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
package org.activityinfo.server.command.handler.crud;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.UpdatePartner;
import org.activityinfo.legacy.shared.command.result.BillingException;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.model.account.AccountStatus;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.server.command.handler.PermissionOracle;
import org.activityinfo.server.command.handler.UpdatePartnerHandler;
import org.activityinfo.server.database.hibernate.dao.CountryDAO;
import org.activityinfo.server.database.hibernate.dao.UserDatabaseDAO;
import org.activityinfo.server.database.hibernate.entity.Country;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;
import org.activityinfo.store.query.UsageTracker;

import javax.persistence.EntityManager;
import java.util.Date;

public class UserDatabasePolicy implements EntityPolicy<Database> {

    private final EntityManager em;
    private final UserDatabaseDAO databaseDAO;
    private final CountryDAO countryDAO;
    private final BillingAccountOracle billingAccounts;

    @Inject
    public UserDatabasePolicy(EntityManager em, UserDatabaseDAO databaseDAO, CountryDAO countryDAO, BillingAccountOracle billingAccounts) {
        this.em = em;
        this.databaseDAO = databaseDAO;
        this.countryDAO = countryDAO;
        this.billingAccounts = billingAccounts;
    }

    @Override
    public Object create(User user, PropertyMap properties) {

        checkBillingLimitations(user);

        Database database = new Database();
        database.setCountry(findCountry(properties));
        database.setOwner(user);

        applyProperties(database, properties);

        UsageTracker.track(user.getId(), "create_database", CuidAdapter.databaseId(database.getId()));

        databaseDAO.persist(database);

        addDefaultPartner(database.getId(), user);

        return database.getId();
    }

    private void checkBillingLimitations(User user) {
        AccountStatus status = billingAccounts.getStatusOrStartFreeTrial(user);

        if(!status.isNewDatabaseAllowed()) {
            throw new BillingException(status);
        }
    }

    private void addDefaultPartner(int databaseId, User user) {
        PartnerDTO partner = new PartnerDTO();
        partner.setName(PartnerDTO.DEFAULT_PARTNER_NAME);

        UpdatePartner command = new UpdatePartner(databaseId, partner);

        new UpdatePartnerHandler(em).execute(command, user);
    }

    public Database findById(int dbId) {
        return databaseDAO.findById(dbId);
    }

    private Country findCountry(PropertyMap properties) {
        int countryId;
        if (properties.containsKey("countryId")) {
            countryId = properties.get("countryId");
        } else {
            // this was the default
            countryId = 1;
        }
        Country country = countryDAO.findById(countryId);
        if(country == null) {
            throw new CommandException(String.format("No country exists with id %d", countryId));
        }
        return country;
    }

    @Override
    public void update(User user, Object entityId, PropertyMap changes) {
        Database database = em.find(Database.class, entityId);
        PermissionOracle.using(em).assertDesignPrivileges(database, user);
        applyProperties(database, changes);
    }

    private void applyProperties(Database database, PropertyMap properties) {

        database.setLastSchemaUpdate(new Date());

        if (properties.containsKey("name")) {
            database.setName((String) properties.get("name"));
        }

        if (properties.containsKey("fullName")) {
            database.setFullName((String) properties.get("fullName"));
        }

        if (properties.containsKey("description")) {
            database.setFullName((String) properties.get("description"));
        }
    }

}
