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
