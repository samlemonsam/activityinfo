package org.activityinfo.geoadmin.merge2.view.model;

/**
 * For cases where the source is being <em>merged</em> into the target, 
 * describes teh matching between the 
 */
public class RowMatching  {
    
    private FormMapping mapping;
    private FormProfile source;
    private FormProfile target;

    public RowMatching(FormMapping mapping) {
        this.mapping = mapping;
        this.source = mapping.getSource();
        this.target = mapping.getTarget();
    }
    
    
}
