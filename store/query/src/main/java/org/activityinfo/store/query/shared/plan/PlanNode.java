package org.activityinfo.store.query.shared.plan;

import org.activityinfo.model.type.FieldType;

/**
 * Describes a node in a directed-acyclic graph (DAG) describe the computation of a query
 * column.
 *
 */
public abstract class PlanNode {

    public abstract FieldType getFieldType();

    public abstract <T> T accept(PlanVisitor<T> visitor);

}
