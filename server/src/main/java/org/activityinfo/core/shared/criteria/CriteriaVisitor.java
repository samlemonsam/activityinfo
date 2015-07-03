package org.activityinfo.core.shared.criteria;

public abstract class CriteriaVisitor {

    public void visitFieldCriteria(FieldCriteria criteria) {

    }

    public void visitClassCriteria(ClassCriteria criteria) {

    }


    public void visitIntersection(CriteriaIntersection intersection) {

    }

    public void visitUnion(CriteriaUnion criteria) {

    }

    public void visitInstanceIdCriteria(IdCriteria criteria) {

    }

    public void visitParentCriteria(ParentCriteria criteria) {

    }

    public void visitFieldCriteria(FieldDateCriteria fieldDateCriteria) {

    }
}
