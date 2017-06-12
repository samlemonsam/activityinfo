package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.resource.ResourceId;

import java.util.Random;

public class ParentGenerator implements Supplier<ResourceId> {

    private final Random random = new Random(529523452L);
    private final TestForm parentForm;

    public ParentGenerator(TestForm parentForm) {
        this.parentForm = parentForm;
    }


    @Override
    public ResourceId get() {
        int index = random.nextInt(parentForm.getRecords().size());
        return parentForm.getRecords().get(index).getId();
    }
}
