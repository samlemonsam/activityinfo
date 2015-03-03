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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSyncRegions;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.SyncRegion;
import org.activityinfo.legacy.shared.command.result.SyncRegions;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.util.CollectionUtil;
import org.activityinfo.server.command.handler.sync.AdminUpdateBuilder;
import org.activityinfo.server.command.handler.sync.TableDefinitionUpdateBuilder;
import org.activityinfo.server.database.hibernate.entity.User;

import javax.persistence.EntityManager;
import java.util.*;

public class GetSyncRegionsHandler implements CommandHandler<GetSyncRegions> {

    private EntityManager entityManager;

    @Inject
    public GetSyncRegionsHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override @SuppressWarnings("unchecked")
    public CommandResult execute(GetSyncRegions cmd, User user) throws CommandException {

        List<Object[]> databases = entityManager.createQuery("SELECT " +
                                                             "db.id, db.country.id, db.version, " +
                                                             "MAX(p.version) " +
                                                             "FROM UserDatabase db " +
                                                             "LEFT JOIN db.userPermissions p " +
                                                             "GROUP BY db.id").getResultList();

        Map<Integer, Long> databaseIdToVerions = Maps.newHashMap();
        Set<Integer> countryIds = Sets.newHashSet();

        for (Object[] db : databases) {
            long version = 1;
            if (db[2] != null) {
                version = (Long) db[2];
            }
            if (db[3] != null && (Long) db[3] > version) {
                version = (Long) db[3];
            }
            databaseIdToVerions.put((Integer) db[0], version);
            countryIds.add((Integer) db[1]);
        }

        List<SyncRegion> regions = Lists.newArrayList();
        regions.add(new SyncRegion("site-tables", TableDefinitionUpdateBuilder.CURRENT_VERSION));
        regions.addAll(listDbs(databaseIdToVerions));
        regions.addAll(listAdminRegions(countryIds));
        regions.addAll(listLocations(countryIds));
        regions.addAll(listSiteRegions(databaseIdToVerions.keySet()));
        return new SyncRegions(regions);
    }

    private Collection<? extends SyncRegion> listDbs(Map<Integer, Long> databaseIdToVerions) {
        List<SyncRegion> dbRegions = Lists.newArrayList();
        for (Map.Entry<Integer, Long> entry : databaseIdToVerions.entrySet()) {
            dbRegions.add(new SyncRegion("db/" + entry.getKey(), entry.getValue().toString()));
        }
        return dbRegions;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends SyncRegion> listLocations(Set<Integer> countryIds) {

        List<SyncRegion> locationRegions = Lists.newArrayList();

        if (CollectionUtil.isNotEmpty(countryIds)) {
            List<Object[]> regions = entityManager.createNativeQuery("SELECT loc.LocationTypeId, MAX(loc.timeEdited) " +
                    "FROM location loc " +
                    "INNER JOIN locationtype t ON loc.LocationTypeId = t.LocationTypeId " +
                    "WHERE loc.LocationId IN (SELECT LocationId FROM site WHERE dateDeleted is null) " +
                    " AND t.countryId IN (:countries) " +
                    "GROUP BY loc.LocationTypeId")
                    .setParameter("countries", countryIds)
                    .getResultList();

            for (Object[] region : regions) {
                locationRegions.add(new SyncRegion("location/" + region[0], region[1].toString()));
            }
        }
        return locationRegions;
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends SyncRegion> listAdminRegions(Set<Integer> countryIds) {

        List<SyncRegion> adminRegions = Lists.newArrayList();

        if (CollectionUtil.isNotEmpty(countryIds)) {
            List<Integer> levels = entityManager.createQuery("SELECT " +
                                                             "level.id " +
                                                             "FROM AdminLevel level " +
                                                             "WHERE level.country.id in (:countries) ")
                                                .setParameter("countries", countryIds)
                                                .getResultList();

            for (Integer level : levels) {
                adminRegions.add(new SyncRegion("admin/" + level,
                        Integer.toString(AdminUpdateBuilder.LAST_VERSION_NUMBER)));
            }
        }
        return adminRegions;
    }

    /**
     * We need a separate sync region for each Partner/UserDatabase combination
     * because we may be given permission to view data at different times.
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends SyncRegion> listSiteRegions(Collection<Integer> databases) {
        List<SyncRegion> siteRegions = Lists.newArrayList();
        
        if (CollectionUtil.isNotEmpty(databases)) {
            // do one sync region per form
            List<Object[]> regions = entityManager.createQuery("SELECT " +
                                                               "s.activity.id, " +
                                                               "MAX(s.timeEdited) " +
                                                               "FROM Site s " +
                                                               "WHERE s.activity.database.id in (:dbs) " +
                                                               "GROUP BY s.activity.id")
                                                  .setParameter("dbs", databases)
                                                  .getResultList();

            for (Object[] region : regions) {
                siteRegions.add(new SyncRegion("form-submissions/" + region[0], region[1].toString()));
            }
        }
        return siteRegions;
    }
}
