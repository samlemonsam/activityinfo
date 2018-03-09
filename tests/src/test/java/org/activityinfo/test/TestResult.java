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


    public List<Attachment> getAttachments() {
        return attachments;
    }

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
    private List<Attachment> attachments;
    
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
            testResult.attachments = attachments;
            return testResult;
        }


        public void attach(String filename, ByteSource contents) {
            attachments.add(new Attachment(filename, contents));
        }
    }
}
