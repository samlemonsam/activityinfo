package org.activityinfo.legacy.shared.command;

/**
 * Executes a PivotSites command with the old handler.
 * 
 * <p>This is a temporary patch until the new query engine is completely and tested and ready
 * for production. At which point this can be ripped out.</p>
 */
public class OldPivotSites extends PivotSites {
    
    public OldPivotSites(PivotSites command) {
        setDimensions(command.getDimensions());
        setFilter(command.getFilter());
        setValueType(command.getValueType());
        setPointRequested(command.isPointRequested());
        
    }
    
}
