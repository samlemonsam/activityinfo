package org.activityinfo.legacy.shared.adapter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.core.client.InstanceQuery;
import org.activityinfo.core.client.QueryResult;
import org.activityinfo.core.shared.Projection;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.Criteria;
import org.activityinfo.core.shared.criteria.IdCriteria;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.TextValue;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Creates a QueryModel from an InstanceQuery
 */
public class InstanceQueryAdapter {

    private ResourceId classId;
    private QueryModel queryModel;
    
    private List<FieldPath> fieldPaths = Lists.newArrayList();

    public QueryModel build(InstanceQuery query) {
        
        if(query.getCriteria() instanceof ClassCriteria) {
            ClassCriteria classCriteria = (ClassCriteria) query.getCriteria();
            classId = classCriteria.getClassId();
            queryModel = new QueryModel(classCriteria.getClassId());
            
        } else if(query.getCriteria() instanceof IdCriteria) {
            IdCriteria criteria = (IdCriteria) query.getCriteria();
            classId = criteria.getFormId();
            queryModel = new QueryModel(classId);
            queryModel.setFilter(Exprs.idEqualTo(criteria.getInstanceIds()));
        }
        
        queryModel = new QueryModel(classId);
        queryModel.selectResourceId().as("_id");
        for (FieldPath fieldPath : query.getFieldPaths()) { 
            fieldPaths.add(fieldPath);
            queryModel.selectExpr(fieldPath.toExpr()).as(fieldPath.toString());
        }
        
        return queryModel;
    }
    
    public QueryModel build(FormClass formClass) {
        this.classId = formClass.getId();
        queryModel = new QueryModel(classId);
        queryModel.selectResourceId().as("_id");
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof GeoPointType) {
                // Callers are expecting to get a single value with a GeoPoint,
                // but the table query engine would be the lat/lng into two seperate values
                // I don't think it's used, so we'll ignore them here.
            } else {
                FieldPath fieldPath = new FieldPath(formField.getId());
                fieldPaths.add(fieldPath);
                queryModel.selectField(formField.getId()).as(fieldPath.toString());
            }
        }
        return queryModel;
    }
    
    public Function<ColumnSet, List<Projection>> toProjections() {
        return new Function<ColumnSet, List<Projection>>() {
            @Nullable
            @Override
            public List<Projection> apply(@Nullable ColumnSet input) {
                return toProjections(input);
            }
        };
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


    public List<FormInstance> toFormInstances(ColumnSet columnSet) {
        ColumnView id = columnSet.getColumnView("_id");

        List<FormInstance> instances = Lists.newArrayList();
        for (int i = 0; i < columnSet.getNumRows(); i++) {
            instances.add(new FormInstance(ResourceId.valueOf(id.getString(i)), classId));
        }

        for (FieldPath fieldPath : fieldPaths) {
            ColumnView view = columnSet.getColumnView(fieldPath.toString());
            if(!fieldPath.isNested()) {
                switch (view.getType()) {
                    case STRING:
                        populateString(fieldPath.getRoot(), view, instances);
                        break;
                    
                    case NUMBER:
                        populateNumber(fieldPath.getRoot(), view, instances);
                        break;
                }
            }
        }
        return instances;
    }

    private void populateString(FieldPath fieldPath, ColumnView view, List<Projection> projections) {
        for (int i = 0; i < view.numRows(); i++) {
            projections.get(i).setValue(fieldPath, TextValue.valueOf(view.getString(i)));
        }
    }

    private void populateString(ResourceId fieldId, ColumnView view, List<FormInstance> instances) {
        for (int i = 0; i < view.numRows(); i++) {
            instances.get(i).set(fieldId, TextValue.valueOf(view.getString(i)));
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

    private void populateNumber(ResourceId fieldId, ColumnView view, List<FormInstance> instances) {
        for (int i = 0; i < view.numRows(); i++) {
            double value = view.getDouble(i);
            if(!Double.isNaN(value)) {
                instances.get(i).set(fieldId, new Quantity(value));
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

    public Function<? super ColumnSet, List<FormInstance>> toFormInstances() {
        return new Function<ColumnSet, List<FormInstance>>() {
            @Nullable
            @Override
            public List<FormInstance> apply(@Nullable ColumnSet input) {
                return toFormInstances(input);
            }
        };
    }
}
