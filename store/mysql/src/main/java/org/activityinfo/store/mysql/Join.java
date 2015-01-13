package org.activityinfo.store.mysql;

/**
 * Created by alex on 1/12/15.
 */
public class Join {
    private Join parent;
    private String key;
    private String joinExpr;

    public Join(Join parent, String key, String joinExpr) {
        this.parent = parent;
        this.key = key;
        this.joinExpr = joinExpr;
    }

    public Join(String key, String joinExpr) {
        this.key = key;
        this.joinExpr = joinExpr;
    }
}
