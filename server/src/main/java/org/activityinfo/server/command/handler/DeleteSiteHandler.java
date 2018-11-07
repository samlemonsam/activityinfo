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
import org.activityinfo.legacy.shared.command.DeleteSite;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.server.database.hibernate.entity.Site;
import org.activityinfo.server.database.hibernate.entity.SiteHistory;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.store.spi.DatabaseProvider;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import java.util.Date;

@SuppressWarnings("unused")
public class DeleteSiteHandler implements CommandHandler<DeleteSite> {
    
    private final EntityManager entityManager;
    private final DatabaseProvider databaseProvider;

    @Inject
    public DeleteSiteHandler(EntityManager entityManager, DatabaseProvider databaseProvider) {
        this.entityManager = entityManager;
        this.databaseProvider = databaseProvider;
    }

    @Override
    public VoidResult execute(DeleteSite cmd, User user) throws CommandException {

        Site site = entityManager.find(Site.class, cmd.getSiteId());
        UserDatabaseMeta databaseMeta = databaseProvider.getDatabaseMetadata(
                CuidAdapter.databaseId(site.getActivity().getDatabase().getId()),
                user.getId());

        if (!PermissionOracle.canEditSite(site.getActivity().getFormId(), site.getPartner().getId(), databaseMeta)) {
            throw new IllegalAccessCommandException("Not authorized to modify sites");
        }

        site.setDateDeleted(new Date());
        site.setVersion(site.getActivity().incrementSiteVersion());

        entityManager.createNativeQuery("update reportingperiod set deleted = 1 WHERE siteId = ?")
                .setParameter(1, site.getId())
                .executeUpdate();


        logHistory(user, site);
        
        return VoidResult.EMPTY;
    }

    private void logHistory(User user, Site site) {
        try {
            JSONObject change = new JSONObject();
            change.put("type", "Boolean");
            change.put("value", true);

            JSONObject changeSet = new JSONObject();
            changeSet.put("_DELETE", change);

            SiteHistory history = new SiteHistory();
            history.setUser(user);
            history.setSite(site);
            history.setInitial(false);
            history.setTimeCreated(System.currentTimeMillis());
            history.setJson(changeSet.toString());
            entityManager.persist(history);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
