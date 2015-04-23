package org.activityinfo.legacy.shared.adapter.bindings;

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.model.DTO;

import java.util.Map;

/**
 * Two-way bridge between FormInstances and Legacy objects
 */
public interface FieldBinding<T extends DTO> {


    /**
     * Updates a FormInstance from a Legacy Model
     *
     * @param instance FormInstance
     * @param model    a Legacy Model
     */
    public void updateInstanceFromModel(FormInstance instance, T model);

    /**
     * Updates the toChangePropertyMap
     *
     * @param instance
     * @param changeMap a name -> property value map used to update the model
     */
    public void populateChangeMap(FormInstance instance, Map<String, Object> changeMap);


}
