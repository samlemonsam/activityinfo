package org.activityinfo.model.query;

import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class QueryModelTest {

    @Test
    public void jacksonTest() throws IOException {

        QueryModel model = new QueryModel(ResourceId.valueOf("XYZ"));
        model.selectResourceId().as("id");
        model.selectResourceId();
        model.selectExpr("foo").as("foo_column");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(model);

        System.out.println(json);

        QueryModel remodel = objectMapper.readValue(json, QueryModel.class);
        assertThat(remodel.getFilter(), Matchers.nullValue());
    }

}