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
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.TextValue;

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
            fieldPaths.add(fieldPath);
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
            projections.add(new Projection(ResourceId.valueOf(id.getString(i)), classId));
        }

        for (FieldPath fieldPath : fieldPaths) {
            ColumnView view = columnSet.getColumnView(fieldPath.toString());
            switch (view.getType()) {
                case STRING:
                    populateString(fieldPath, view, projections);
                    break;
                case NUMBER:
                    populateNumber(fieldPath, view, projections);
                    break;
                case BOOLEAN:
                    populateBoolean(fieldPath, view, projections);
                    break;
            }
        }
        
        
        return projections;
    }


    private void populateString(FieldPath fieldPath, ColumnView view, List<Projection> projections) {
        for (int i = 0; i < view.numRows(); i++) {
            projections.get(i).setValue(fieldPath, TextValue.valueOf(view.getString(i)));
        }
    }

    private void populateNumber(FieldPath fieldPath, ColumnView view, List<Projection> projections) {
        for (int i = 0; i < view.numRows(); i++) {
            double value = view.getDouble(i);
            if(!Double.isNaN(value)) {
                projections.get(i).setValue(fieldPath, new Quantity(value));
            }
        }
    }

    private void populateBoolean(FieldPath fieldPath, ColumnView view, List<Projection> projections) {
        for (int i = 0; i < view.numRows(); i++) {
            int value = view.getBoolean(i);
            if(value != ColumnView.NA) {
                projections.get(i).setValue(fieldPath, BooleanFieldValue.valueOf(value == ColumnView.TRUE));
            }
        }
    }


    public QueryResult<Projection> toQueryResult(ColumnSet columnSet) {
        throw new UnsupportedOperationException();
    }
}
