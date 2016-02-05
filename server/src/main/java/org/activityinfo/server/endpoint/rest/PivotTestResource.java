package org.activityinfo.server.endpoint.rest;

import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.model.ReportDTO;
import org.activityinfo.legacy.shared.reports.model.PivotReportElement;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.authentication.ServerSideAuthProvider;
import org.activityinfo.server.command.DispatcherSync;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON)
    public List<PivotSites.PivotResult> get(@QueryParam("reportId") int reportId,
                                            @QueryParam("userId") int userId,
                                            @QueryParam("new") boolean newEngine) {


        assertRunningInLocalDevelopmentEnvironment();
        
        authProvider.set(new AuthenticatedUser("XYZ", userId, "user@user.org"));
        
        ReportDTO report = dispatcher.execute(new GetReportModel(reportId));
        Report model = report.getReport();
        
        List<PivotSites.PivotResult> results = Lists.newArrayList();

        for (ReportElement element : model.getElements()) {
            if (element instanceof PivotReportElement) {
                results.add((PivotSites.PivotResult) dispatcher.execute(
                        command(model, (PivotReportElement) element, newEngine)));
            }
        }
        return results;
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
    
    private Command command(Report model, PivotReportElement<?> element, boolean newEngine) {
        Filter effectiveFilter = new Filter(model.getFilter(), element.getFilter());

        PivotSites command = new PivotSites(element.allDimensions(), effectiveFilter);
        if (newEngine) {
            return command;
        } else {
            return new OldPivotSites(command);
        }
    }
    
}
