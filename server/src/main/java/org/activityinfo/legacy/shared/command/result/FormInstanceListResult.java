package org.activityinfo.legacy.shared.command.result;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.Resources;

import java.util.List;

/**
 * Created by yuriy on 3/1/2015.
 */
public class FormInstanceListResult implements CommandResult {

    private List<String> formInstanceJsonList = Lists.newArrayList();

    public FormInstanceListResult() {
    }

    public FormInstanceListResult(List<String> formInstanceJsonList) {
        this.formInstanceJsonList = formInstanceJsonList;
    }

    public List<String> getFormInstanceJsonList() {
        return formInstanceJsonList;
    }

    public List<FormInstance> getFormInstanceList() {
        List<FormInstance> result = Lists.newArrayList();
        for (String json : formInstanceJsonList) {
            result.add(FormInstance.fromResource(Resources.fromJson(json)));
        }
        return result;
    }

    public FormInstanceListResult setFormInstanceJsonList(List<String> formInstanceJsonList) {
        this.formInstanceJsonList = formInstanceJsonList;
        return this;
    }
}
