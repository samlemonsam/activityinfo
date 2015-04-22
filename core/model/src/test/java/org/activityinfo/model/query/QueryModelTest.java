package org.activityinfo.model.query;

import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class QueryModelTest {

    @Test
    public void test() throws IOException {
        
        QueryModel queryModel = new QueryModel(ResourceId.generateId());
        queryModel.selectExpr("a+1");
        queryModel.selectField(ResourceId.generateId());
        queryModel.selectClassId();
        queryModel.selectField(new FieldPath(ResourceId.generateId(), ResourceId.generateId()));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(queryModel);
        System.out.println(json);
        
        QueryModel remodel = objectMapper.readValue(json, QueryModel.class);
    }
    
}