package org.activityinfo.server.command.handler;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSiteHistory;
import org.activityinfo.legacy.shared.command.GetSiteHistory.GetSiteHistoryResult;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.model.SiteHistoryDTO;
import org.activityinfo.server.database.hibernate.entity.Site;
import org.activityinfo.server.database.hibernate.entity.SiteHistory;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.logging.Logger;

public class GetSiteHistoryHandler implements CommandHandler<GetSiteHistory> {

    private static final Logger LOGGER = Logger.getLogger(GetSiteHistoryHandler.class.getName());
    
    private EntityManager entityManager;
    private PermissionOracle permissionOracle;

    @Inject
    public GetSiteHistoryHandler(EntityManager entityManager, PermissionOracle permissionOracle) {
        this.entityManager = entityManager;
        this.permissionOracle = permissionOracle;
    }

    @Override
    public CommandResult execute(GetSiteHistory cmd, User user) throws CommandException {

        Site site = entityManager.getReference(Site.class, cmd.getSiteId());
        if(!permissionOracle.isViewAllowed(site, user)) {
            LOGGER.severe(user + " does not have access to site " + site.getId());
            throw new IllegalAccessCommandException();
        }

        List<SiteHistory> rows = entityManager.createQuery(
                "SELECT h FROM SiteHistory h JOIN FETCH h.user where h.site = :site", SiteHistory.class)
                .setParameter("site", site)
                .getResultList();


        List<SiteHistoryDTO> changes = Lists.newArrayList();
        for (SiteHistory row : rows) {
            SiteHistoryDTO change = new SiteHistoryDTO();
            change.setId(row.getId());
            change.setTimeCreated(row.getTimeCreated());
            change.setInitial(row.isInitial());
            change.setJson(row.getJson());
            change.setUserName(row.getUser().getName());
            change.setUserEmail(row.getUser().getEmail());
            changes.add(change);
        }
        
        return new GetSiteHistoryResult(changes);
    }

}
