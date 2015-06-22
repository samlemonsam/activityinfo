package org.activityinfo.test;

import org.activityinfo.test.config.ConfigurationError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import static java.lang.String.format;

public class CiTestReporter implements TestReporter {
    
    private String testSuite;
    private File outputDir;
    private Document resultDoc;
    
    private long startTime;
    private TestStats stats;

    public CiTestReporter(File outputDir, TestStats stats) {
        this.outputDir = outputDir;
        this.stats = stats;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            resultDoc  = docBuilder.newDocument();
            Element rootElement = resultDoc.createElement("testsuite");
            resultDoc.appendChild(rootElement);
            
        } catch (Exception e) {
            throw new ConfigurationError("Failed to create XML Test Result Document", e);
        }
    
    }

    @Override
    public void testSuiteStarted(String suiteName) {
        this.testSuite = suiteName;
    }

    @Override
    public void testStarted(String name) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFinished(String name, boolean passed, String output) {
        
        long testTime = System.currentTimeMillis() - startTime;
        
        stats.recordResult(testSuite, name, passed);
        writeConsoleUpdate(name, passed);
        addTestResult(name, passed, testTime, output);    
        
    }

    private void writeConsoleUpdate(String name, boolean passed) {
        System.out.println(format("%5s %s // %s",
                (passed ? "OK" : "FAIL" ), testSuite, name));
    }

    private void addTestResult(String name, boolean passed, long testTime, String output) {
        Element testCase = resultDoc.createElement("testcase");
        testCase.setAttribute("classname", testSuite);
        testCase.setAttribute("name", name);
        testCase.setAttribute("time", Long.toString(testTime));

        if(!passed) {
            Element failure = resultDoc.createElement("failure");
            failure.setAttribute("message", "Test Failure");
            testCase.appendChild(failure);
        }

        Element systemOut = resultDoc.createElement("system-out");
        systemOut.appendChild(resultDoc.createCDATASection(output));
        testCase.appendChild(systemOut);
        
        resultDoc.getDocumentElement().appendChild(testCase);
    }

    @Override
    public void testSuiteFinished() {
        writeXml();        
    }

    private void writeXml()  {

        File outputFile = new File(outputDir, "TEST-" + testSuite + ".xml");
        
        System.out.println("Writing " + outputFile.getAbsolutePath());
        
        
        try {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(resultDoc);
            StreamResult result = new StreamResult(outputFile);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (Exception e) {
            throw new ConfigurationError("Failed to write test result file", e);
        }
    }
}
