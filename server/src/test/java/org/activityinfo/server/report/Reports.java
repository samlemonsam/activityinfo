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
package org.activityinfo.server.report;

import org.activityinfo.TestOutput;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.server.geo.TestGeometry;
import org.activityinfo.server.report.renderer.itext.HtmlReportRenderer;
import org.activityinfo.server.report.renderer.itext.PdfReportRenderer;
import org.activityinfo.server.report.renderer.itext.RtfReportRenderer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Reports {

    private static final String MAP_ICON_PATH = "";


    public static void toPdf(Class<?> testClass, ReportElement report, String fileName) throws IOException {
        PdfReportRenderer renderer = new PdfReportRenderer(TestGeometry.get(), MAP_ICON_PATH);
        try(FileOutputStream out = TestOutput.open(testClass, fileName + ".pdf")) {
            renderer.render(report, out);
        }   
    }

    public static void toPdf(Class<?> testClass, ReportElement report) throws IOException {
        toPdf(testClass, report, "report");
    }
    
    public static void toRtf(Class<?> testClass, ReportElement report) throws IOException {
        RtfReportRenderer renderer = new RtfReportRenderer(TestGeometry.get(), MAP_ICON_PATH);
        try(FileOutputStream out = TestOutput.open(testClass, "report.rtf")) {
            renderer.render(report, out);
        }
    }

    public static void toHtml(Class<? extends StaticElementRenderTest> testClass, Report report) throws IOException {
        HtmlReportRenderer renderer = 
                new HtmlReportRenderer(TestGeometry.get(), MAP_ICON_PATH, new NullStorageProvider());
        try(FileOutputStream out = TestOutput.open(testClass, "report.html")) {
            renderer.render(report, out);
        }
    }


    public static Report parseXml(Class<?> testClass, String filename) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(Report.class.getPackage()
                .getName());
        Unmarshaller um = jc.createUnmarshaller();
        um.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
        return (Report) um.unmarshal(new InputStreamReader(
                testClass.getResourceAsStream("/report-def/parse-test/" + filename)));
    }

}
