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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * Records overall test results for an acceptance test run
 */
public class TestStats {

    private int count;
    private int failureCount = 0;
    private List<TestResult> results = new ArrayList<>();
    
    private long totalRunningTime = 0;
    
    private long wallTime;
    
    private long startTime = System.currentTimeMillis();
    
    public void recordTime(TestResult result) {
        totalRunningTime += result.getDuration(TimeUnit.MILLISECONDS);
    }
    
    public synchronized void recordResult(TestResult result) {
        count++;
        if(!result.isPassed()) {
            failureCount++;
        }
        results.add(result);
    }

    public List<TestResult> getResults() {
        return results;
    }

    public void finished() {
        wallTime = System.currentTimeMillis() - startTime;
    }
    
    public void printSummary() {
        System.out.println();
        System.out.println(format("Finished in %d minutes, total running time was %d minutes. (Parallelism: %.1f)", 
                TimeUnit.MILLISECONDS.toMinutes(wallTime),
                TimeUnit.MILLISECONDS.toMinutes(totalRunningTime),
                (double)totalRunningTime / (double)wallTime));

        System.out.println(format("TEST RESULTS: %d Tests, %d Failed.", count, failureCount));

    }
    
    public boolean hasFailures() {
        return failureCount > 0;
    }

}
