package org.activityinfo.test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The result of an individual test run
 */
public class TestResult {




    public static class Attachment {
        private String filename;
        private ByteSource byteSource;

        public Attachment(String filename, ByteSource byteSource) {
            this.filename = filename;
            this.byteSource = byteSource;
        }

        public String getFilename() {
            return filename;
        }

        public ByteSource getByteSource() {
            return byteSource;
        }
    }

    private TestCase testCase;
    private boolean passed;
    private String output;
    private long duration;
    
    private TestResult() {
    }

    public String getId() {
        return testCase.getId();
    }
    
    public long getDuration(TimeUnit timeUnit) {
        return timeUnit.convert(duration, TimeUnit.MILLISECONDS);
    }
    
    public boolean isPassed() {
        return passed;
    }

    public String getOutput() {
        return output;
    }

    public long getDuration() {
        return duration;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public static Builder builder(TestCase testCase) {
        return new Builder(testCase);
    }

    public static class Builder {
        private final StringBuilder output = new StringBuilder();
        private final List<Attachment> attachments = Lists.newArrayList();
        private final Stopwatch stopwatch = Stopwatch.createStarted();

        private boolean passed = true;
        private TestCase testCase;

        private Builder(TestCase testCase) {
            this.testCase = testCase;
        }

        public Builder failed() {
            this.passed = false;
            return this;
        }

        public StringBuilder output() {
            return output;
        }

        public TestResult build() {
            TestResult testResult = new TestResult();
            testResult.testCase = testCase;
            testResult.duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            testResult.passed = passed;
            testResult.output = output.toString();
            return testResult;
        }


    }
}
