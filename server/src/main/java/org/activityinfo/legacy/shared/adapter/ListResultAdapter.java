package org.activityinfo.legacy.shared.adapter;


import com.extjs.gxt.ui.client.data.ModelData;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.legacy.shared.command.result.ListResult;

import java.util.List;

/**
 * Transforms each element in a legacy {@code ListResult} to a FormInstance using
 * the provided InstanceAdapter
 *
 * @param <T>
 */
public class ListResultAdapter<T extends ModelData> implements Function<ListResult<T>, List<FormInstance>> {

    private final Function<T, FormInstance> instanceAdapter;

    public ListResultAdapter(Function<T, FormInstance> instanceAdapter) {
        this.instanceAdapter = instanceAdapter;
    }

    @Override
    public List<FormInstance> apply(ListResult<T> input) {
        return Lists.transform(input.getData(), instanceAdapter);
    }
}
