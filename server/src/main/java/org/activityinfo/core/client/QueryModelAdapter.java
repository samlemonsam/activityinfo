package org.activityinfo.core.client;

import com.google.common.collect.Lists;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

/**
 * Creates a QueryModel from an InstanceQuery
 */
public class QueryModelAdapter {
    
    private ResourceId classId;
    private QueryModel queryModel;
    
    private List<FieldPath> fieldPaths = Lists.newArrayList();
    
    public QueryModel build(InstanceQuery query) {
        
        buildCriteria(query.getCriteria());
        
        queryModel = new QueryModel(classId);
        queryModel.selectResourceId().as("_id");
        for (FieldPath fieldPath : query.getFieldPaths()) { 
            queryModel.selectExpr(fieldPath.toExpr()).as(fieldPath.toString());
        }
        
        return queryModel;
    }

    private void buildCriteria(Criteria criteria) {
        if(criteria instanceof ClassCriteria) {
            classId = ((ClassCriteria) criteria).getClassId();
        } else {
            throw new UnsupportedOperationException("criteria: " + criteria);
        }
    }

    public List<Projection> toProjections(ColumnSet columnSet) {

        ColumnView id = columnSet.getColumnView("_id");
        
        List<Projection> projections = Lists.newArrayList();
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            Projection projection = new Projection(ResourceId.valueOf(id.getString(i)), classId);
            
            projections.add(projection);
        }
        return projections;
    }

    public QueryResult<Projection> toQueryResult(ColumnSet columnSet) {
        throw new UnsupportedOperationException();
    }
}
