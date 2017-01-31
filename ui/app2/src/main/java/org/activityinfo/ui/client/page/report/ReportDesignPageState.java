package org.activityinfo.ui.client.page.report;

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

import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.PageStateParser;
import org.activityinfo.ui.client.page.app.Section;

import java.util.Arrays;
import java.util.List;

public class ReportDesignPageState implements PageState {

    private int reportId;

    public ReportDesignPageState() {

    }

    public ReportDesignPageState(int reportId) {
        this.reportId = reportId;
    }

    @Override
    public PageId getPageId() {
        return ReportDesignPage.PAGE_ID;
    }

    @Override
    public String serializeAsHistoryToken() {
        return Integer.toString(reportId);
    }

    @Override
    public List<PageId> getEnclosingFrames() {
        return Arrays.asList(ReportDesignPage.PAGE_ID);
    }

    public int getReportId() {
        return reportId;
    }

    public static class Parser implements PageStateParser {
        @Override
        public PageState parse(String token) {
            return new ReportDesignPageState(Integer.parseInt(token));
        }
    }

    @Override
    public Section getSection() {
        return Section.ANALYSIS;
    }
}
