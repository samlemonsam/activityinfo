package org.activityinfo.test;

import com.google.common.collect.Maps;

import java.util.Map;


/**
 * Keeps track of how many times a particular test case has been attempted
 */
public class RetryCounter {
    /**
     * Map from test id to 
     */
    private Map<String, Integer> attemptCounts = Maps.newHashMap();
    
    private Map<TestResult, Integer> attemptIndexMap = Maps.newHashMap();
    
    
    public int getAttemptNumber(TestResult result) {
        Integer attemptIndex = attemptIndexMap.get(result);
        if(attemptIndex == null) {
            attemptIndex = attemptCounts.get(result.getId());
            if(attemptIndex == null) {
                attemptIndex = 0;
            }
            attemptCounts.put(result.getId(), attemptIndex+1);
            attemptIndexMap.put(result, attemptIndex);
        }
        return attemptIndex;
    }
}
