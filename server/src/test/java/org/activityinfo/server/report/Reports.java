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
