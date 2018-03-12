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
import org.activityinfo.legacy.shared.command.CreateReport;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.exception.ParseException;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.activityinfo.server.database.hibernate.entity.ReportDefinition;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.report.ReportParserJaxb;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;

public class CreateReportHandler implements CommandHandler<CreateReport> {
    private EntityManager em;

    @Inject
    public CreateReportHandler(EntityManager em) {
        this.em = em;
    }

    @Override
    public CommandResult execute(CreateReport cmd, User user) {

        // verify that the XML is valid
        try {
            ReportDefinition reportDef = new ReportDefinition();

            String xml = ReportParserJaxb.createXML(cmd.getReport());
            reportDef.setXml(xml);

            if (cmd.getDatabaseId() != null) {
                reportDef.setDatabase(em.getReference(Database.class, cmd.getDatabaseId()));
            }

            reportDef.setTitle(cmd.getReport().getTitle());
            reportDef.setDescription(cmd.getReport().getDescription());
            reportDef.setOwner(user);
            reportDef.setVisibility(1);

            em.persist(reportDef);

            return new CreateResult(reportDef.getId());

        } catch (JAXBException e) {
            throw new ParseException(e.getMessage());
        }

    }
}
