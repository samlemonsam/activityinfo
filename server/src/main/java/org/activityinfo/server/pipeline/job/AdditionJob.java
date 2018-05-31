package org.activityinfo.server.pipeline.job;

import com.google.appengine.tools.pipeline.Job2;
import com.google.appengine.tools.pipeline.Value;

/**
 * Simple arithmetic addition class for testing and proof-of-concept
 */
public class AdditionJob extends Job2<Integer, Integer, Integer> {

    @Override
    public Value<Integer> run(Integer a, Integer b) throws Exception {
        return immediate(a + b);
    }
}
