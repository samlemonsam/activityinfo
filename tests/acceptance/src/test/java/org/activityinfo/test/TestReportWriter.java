package org.activityinfo.test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestReportWriter {
    
    private File outputDir;
  
    /**
     *
     * @param outputDir the directory to which test results should be written
     */
    public TestReportWriter(File outputDir) {
        this.outputDir = outputDir;
    }

    public void write(List<TestResult> results) {
        Multimap<String, TestResult> suiteMap = HashMultimap.create();
        for (TestResult result : results) {
            suiteMap.put(suiteName(result), result);
        }
        for (String suite : suiteMap.keySet()) {
            writeSuite(suite, suiteMap.get(suite));
        }
    }

    private String suiteName(TestResult result) {
        String id = result.getId();
        int lastDot = id.lastIndexOf('.');
        if(lastDot == -1) {
            throw new IllegalStateException("Test id is not qualified: " + id);
        }
        return id.substring(0, lastDot);
    }

    private String testName(TestResult testResult) {
        String id = testResult.getId();
        int lastDot = id.lastIndexOf('.');
        if(lastDot == -1) {
            throw new IllegalStateException("Test id is not qualified: " + id);
        }
        return id.substring(lastDot+1);
    }


    private void writeSuite(String suiteName, Collection<TestResult> testResults) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document resultDoc = docBuilder.newDocument();
            Element rootElement = resultDoc.createElement("testsuite");
            resultDoc.appendChild(rootElement);

            for (TestResult testResult : testResults) {
                addTestResult(rootElement, testResult);
            }

            writeXml(resultDoc, suiteName);
            
        } catch (Exception e) {
            throw new ConfigurationError("Failed to create XML Test Result Document", e);
        }
    }
    
    private void addTestResult(Element testSuite, TestResult testResult) {
        Document resultDoc = testSuite.getOwnerDocument();
        Element testCase = resultDoc.createElement("testcase");
        testCase.setAttribute("classname", suiteName(testResult));
        testCase.setAttribute("name", testName(testResult));
        testCase.setAttribute("time", Long.toString(testResult.getDuration(TimeUnit.SECONDS)));

        if(!testResult.isPassed()) {
            Element failure = resultDoc.createElement("failure");
            failure.setAttribute("message", "Test Failure");
            testCase.appendChild(failure);
        }

        Element systemOut = resultDoc.createElement("system-out");
        systemOut.appendChild(resultDoc.createCDATASection(testResult.getOutput()));
        testCase.appendChild(systemOut);
        
        resultDoc.getDocumentElement().appendChild(testCase);
    }
    
    private File attachmentDir(String testSuite) {
        File dir = new File(outputDir, testSuite);
        if(!dir.exists()) {
            boolean created = dir.mkdirs();
            if(!created) {
                throw new IllegalStateException("Could not create attachment dir " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    private void writeXml(Document resultDoc, String suiteName)  {

        File outputFile = new File(outputDir, "TEST-" + suiteName + ".xml");
        
        try(FileOutputStream out = new FileOutputStream(outputFile)) {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(resultDoc);

            // Output to console for testing
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);

        } catch (Exception e) {
            throw new ConfigurationError("Failed to write test result file", e);
        }
    }
}
