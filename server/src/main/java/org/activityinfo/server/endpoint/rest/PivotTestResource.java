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
package org.activityinfo.server.endpoint.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.model.ReportDTO;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.command.DispatcherSync;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * Endpoint for testing results of old vs new pivot table engine
 */
public class PivotTestResource {

    private DispatcherSync dispatcher;
    private ServerSideAuthProvider authProvider;
    
    public PivotTestResource(DispatcherSync dispatcher, ServerSideAuthProvider authProvider) {
        this.dispatcher = dispatcher;
        this.authProvider = authProvider;
    }

    @GET
    @Path("report")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PivotSites.PivotResult> get(@QueryParam("reportId") int reportId,
                                            @QueryParam("userId") int userId,
                                            @QueryParam("new") boolean newEngine,
                                            @QueryParam("details") boolean showDetails) {


        assertRunningInLocalDevelopmentEnvironment();
        
        authProvider.set(new AuthenticatedUser("XYZ", userId, "user@user.org"));
        
        ReportDTO report = dispatcher.execute(new GetReportModel(reportId));
        Report model = report.getReport();
        
        List<PivotSites.PivotResult> results = Lists.newArrayList();

        for (ReportElement element : model.getElements()) {
            if (element instanceof PivotReportElement) {
                PivotSites command = command(model, (PivotReportElement) element, newEngine, showDetails);
                if (!command.isTooBroad()) {
                    results.add(dispatcher.execute(command));
                } else {
                    results.add(new PivotSites.PivotResult());
                }
            }
        }
        return results;
    }

    @GET
    @Path("database")
    @Produces(MediaType.APPLICATION_JSON) 
    public List<PivotSites.PivotResult> getDatabase(@QueryParam("databaseId") int databaseId,
                                            @QueryParam("userId") int userId,
                                            @QueryParam("targets") boolean targets,
                                            @QueryParam("partners") boolean partner,
                                            @QueryParam("projects") boolean project,
                                            @QueryParam("details") boolean details,
                                            @QueryParam("new") boolean newEngine) {


        assertRunningInLocalDevelopmentEnvironment();

        authProvider.set(new AuthenticatedUser("XYZ", userId, "user@user.org"));

        Filter filter = new Filter();
        filter.addRestriction(DimensionType.Database, databaseId);

        Set<Dimension> dimensions = Sets.newHashSet();
        dimensions.add(new Dimension(DimensionType.Activity));
        dimensions.add(new Dimension(DimensionType.Indicator));
        if(targets) {
            dimensions.add(new Dimension(DimensionType.Target));
        }
        if(partner) {
            dimensions.add(new Dimension(DimensionType.Partner));
        }
        if(project) {
            dimensions.add(new Dimension(DimensionType.Project));
        }
        
        if(details) {
            dimensions.add(new Dimension(DimensionType.Site));
        }
        
        PivotSites command = new PivotSites(dimensions, filter);
        if(!newEngine) {
            command = new OldPivotSites(command);
        }

        return Lists.newArrayList(command.isTooBroad() ? new PivotSites.PivotResult() : dispatcher.execute(command));
    }

    @GET
    @Path("show")
    @Produces(MediaType.TEXT_PLAIN) 
    public String show(@QueryParam("reportId") int reportId) {

        assertRunningInLocalDevelopmentEnvironment();

        ReportDTO report = dispatcher.execute(new GetReportModel(reportId));
        Report model = report.getReport();
     
        StringBuilder text = new StringBuilder();
        text.append(model.getTitle()).append("\n");
        text.append("Filter: " + model.getFilter()).append("\n");


        for (ReportElement element : model.getElements()) {
            text.append("-- " + element.getClass().getSimpleName() + ": " + element.getTitle()).append("\n");
            text.append("   Filter: " + element.getFilter()).append("\n");
            if(element instanceof PivotReportElement) {
                text.append("   Dimensions: " + ((PivotReportElement) element).allDimensions()).append("\n");
            }
        }
        return text.toString();
    }

    /**
     * ONLY enable in local development version
     */
    private void assertRunningInLocalDevelopmentEnvironment() {
        if(!DeploymentEnvironment.isAppEngineDevelopment()) {
            throw new WebApplicationException(Response.Status.SERVICE_UNAVAILABLE);
        }
    }

    private PivotSites command(Report model, PivotReportElement<?> element, boolean newEngine, boolean showDetails) {
        Filter effectiveFilter = new Filter(model.getFilter(), element.getFilter());

        Set<Dimension> dimensions = Sets.newHashSet();
        for (Dimension dimension : element.allDimensions()) {
            dimensions.add(dimension);
        }
        
        if(showDetails) {
            dimensions.add(new Dimension(DimensionType.Site));
            dimensions.add(new Dimension(DimensionType.Activity));
            dimensions.add(new Dimension(DimensionType.Database));
            dimensions.add(new Dimension(DimensionType.Indicator));
        }
        
        PivotSites command = new PivotSites(dimensions, effectiveFilter);
        if (newEngine) {
            return command;
        } else {
            return new OldPivotSites(command);
        }
    }
    

}
