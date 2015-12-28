package org.activityinfo.core.shared.criteria;

public abstract class CriteriaVisitor {

    public void visitFieldCriteria(FieldCriteria criteria) {

    }

    public void visitClassCriteria(ClassCriteria criteria) {

    }


    public void visitIntersection(CriteriaIntersection intersection) {
        for (Criteria criteria : intersection) {
            criteria.accept(this);
        }
    }

    public void visitUnion(CriteriaUnion unionCriteria) {
        for (Criteria criteria : unionCriteria) {
            criteria.accept(this);
        }
    }

    public void visitInstanceIdCriteria(IdCriteria criteria) {

    }

    public void visitParentCriteria(ParentCriteria criteria) {

    }

    public void visitFieldCriteria(FieldDateCriteria fieldDateCriteria) {

    }
}
