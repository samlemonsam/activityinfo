package org.activityinfo.server.command.handler;

import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GenerateDashboardElement;
import org.activityinfo.legacy.shared.command.GetReportModel;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.model.ReportDTO;
import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.legacy.shared.reports.model.TextReportElement;
import org.activityinfo.server.command.DispatcherSync;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.report.generator.ReportGenerator;

public class GenerateDashboardElementHandler implements CommandHandler<GenerateDashboardElement> {

    
    private DispatcherSync dispatcher;
    private final ReportGenerator generator;

    @Inject
    public GenerateDashboardElementHandler(ReportGenerator generator, DispatcherSync dispatcher) {
        this.generator = generator;
        this.dispatcher = dispatcher;
    }

    @Override
    public CommandResult execute(GenerateDashboardElement cmd, User user) throws CommandException {
        ReportDTO report = dispatcher.execute(new GetReportModel(cmd.getReportId()));
        if(report.getReport().getElements().isEmpty()) {
            return new TextReportElement("The report is empty");
        } else {
            ReportElement element = report.getReport().getElements().get(0);
            element.setContent(generator.generateElement(user, element, null, new DateRange()));
            return element;
        }
    }
}
