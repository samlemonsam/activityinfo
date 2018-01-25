package org.activityinfo.model.query;

import org.activityinfo.json.Json;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

public class QueryModelTest {

    @Test
    public void clientSideSerialization() {
        QueryModel model = new QueryModel(ResourceId.valueOf("XYZ"));
        model.selectResourceId().as("id");
        model.selectResourceId();
        model.selectExpr("foo").as("foo_column");

        String json = model.toJsonString();

        System.out.println(json);

        QueryModel remodel = QueryModel.fromJson(Json.parse(json));
    }

}