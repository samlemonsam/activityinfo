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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetReportModel;
import org.activityinfo.legacy.shared.command.UpdateReportModel;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.exception.IllegalAccessCommandException;
import org.activityinfo.legacy.shared.exception.UnexpectedCommandException;
import org.activityinfo.server.database.hibernate.entity.ReportDefinition;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.report.ReportParserJaxb;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateReportModelHandler implements CommandHandler<UpdateReportModel> {

    private static final Logger LOGGER = Logger.getLogger(UpdateReportModelHandler.class.getName());

    private static final MemcacheService MEMCACHE = MemcacheServiceFactory.getMemcacheService();

    private final EntityManager em;

    @Inject
    public UpdateReportModelHandler(final EntityManager em) {
        this.em = em;
    }

    @Override
    public CommandResult execute(final UpdateReportModel cmd, final User user) throws CommandException {

        Query query = em.createQuery("select r from ReportDefinition r where r.id in (:id)")
                        .setParameter("id", cmd.getModel().getId());

        ReportDefinition result = (ReportDefinition) query.getSingleResult();
        if (result.getOwner().getId() != user.getId()) {
            throw new IllegalAccessCommandException("Current user does not have the right to edit this report");
        }

        // Invalidate the cache BEFORE attempting to update the database,
        // otherwise, we will leave the system in an inconsistent state if 
        // the database update succeeds, but the memcache delete fails.
        invalidateMemcache(cmd.getModel().getId());

        // Now that we're sure that the memcache is clear of the old copy,
        // we can safely update the underlying persistant datastore
        result.setTitle(cmd.getModel().getTitle());
        try {
            result.setXml(ReportParserJaxb.createXML(cmd.getModel()));
        } catch (JAXBException e) {
            throw new UnexpectedCommandException(e);
        }
        em.persist(result);

        return null;
    }

    public static void invalidateMemcache(Integer reportId) {
        
        // Invalidate the existing memcache entries for this reportId,
        // AND prevent memcache from accepting updates for 10 seconds.
        // This will ensure that any concurrent reads do not cache an old 
        // version before the database commit completes.
        try {
            MEMCACHE.deleteAll(Arrays.asList(
                    new GetReportModel(reportId, false),
                    new GetReportModel(reportId, true)), TimeUnit.SECONDS.toMillis(30));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to invalidate report cache", e);
        }
    }

}
