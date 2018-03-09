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
package org.activityinfo.legacy.shared.command;

import org.activityinfo.legacy.shared.model.ReportDTO;

/**
 * Retrieves a report model from the server
 */
public class GetReportModel implements Command<ReportDTO> {

    private Integer reportId;
    private boolean loadMetadata = false;

    public GetReportModel() {
    }

    public GetReportModel(boolean loadMetadata) {
        this.loadMetadata = loadMetadata;
    }

    public GetReportModel(Integer reportId) {
        this.reportId = reportId;
    }

    public GetReportModel(Integer reportId, boolean loadMetadata) {
        this.reportId = reportId;
        this.loadMetadata = loadMetadata;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public boolean isLoadMetadata() {
        return loadMetadata;
    }

    public void setLoadMetadata(boolean loadMetadata) {
        this.loadMetadata = loadMetadata;
    }
}
