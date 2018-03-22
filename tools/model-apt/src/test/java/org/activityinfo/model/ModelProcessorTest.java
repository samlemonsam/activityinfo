package org.activityinfo.model;

import org.junit.Test;

public class ModelProcessorTest {

    @Test
    public void test() {
        DummyModel model = new DummyModel();
        DummyModelJson.toJson(model);
    }
}
