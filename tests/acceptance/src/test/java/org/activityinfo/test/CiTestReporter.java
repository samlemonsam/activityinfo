package org.activityinfo.test;

import com.google.common.io.Files;
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
import java.io.IOException;
import java.io.PrintStream;

import static java.lang.String.format;

public class CiTestReporter implements TestReporter {
    
    private String testSuite;
    private String environment;
    private File outputDir;
    private Document resultDoc;
    
    private long startTime;
    private TestStats stats;
    private PrintStream out;
    
    private int attachmentIndex = 1;

    /**
     * 
     * @param environment a string describing the environment in which the test is run (e.g. chrome, phantomjs,
     *                    sauce-chrome-winxp, etc)
     * @param outputDir the directory to which test results should be written
     * @param stats global test stat collector
     */
    public CiTestReporter(String environment, File outputDir, TestStats stats) {
        this.environment = environment;
        this.outputDir = outputDir;
        this.stats = stats;
        this.out = TestOutputStream.getStandardOutput();
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
        this.testSuite = suiteName + "." + environment;
        Thread.currentThread().setName("Test Suite: " + suiteName);
    }

    @Override
    public void testStarted(String name) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void testFinished(String name, boolean passed, String output) {
        
        long testTime = System.currentTimeMillis() - startTime;
        
        stats.recordResult(testSuite, name, testTime, passed);
        writeConsoleUpdate(name, passed, testTime);
        addTestResult(name, passed, testTime, output);    
        
    }

    private void writeConsoleUpdate(String name, boolean passed, double testTime) {
        out.println(format("%5s %s : %s (%.1f s)",
                (passed ? "OK" : "FAIL"), testSuite, name, testTime / 1000d));
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

    @Override
    public void attach(String mimeType, byte[] data) {
        File attachmentFile = new File(attachmentDir(), nextFilename(mimeType));
        try {
            Files.write(data, attachmentFile);
        } catch (IOException e) {
            throw new RuntimeException("Exception write test attachment");
        }
    }

    private File attachmentDir() {
        File dir = new File(outputDir, testSuite);
        if(!dir.exists()) {
            boolean created = dir.mkdirs();
            if(!created) {
                throw new IllegalStateException("Could not create attachment dir " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    private String nextFilename(String mimeType) {
        if(mimeType.equals("image/png")) {
            return "image" + (attachmentIndex++) + ".png";
        } else {
            return "attachment" + (attachmentIndex++);
        }
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
