package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public class BlankSubForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("BLANK_SUB_FORM");

    public static final int ROW_COUNT = IncidentForm.ROW_COUNT;

    private final GenericForm parentForm;
    private final FormClass formClass;

    private List<FormInstance> records = null;
    private RecordGenerator generator;

    public BlankSubForm(GenericForm parentForm) {
        this.parentForm = parentForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Blank Sub-Form");
        formClass.setSubFormKind(SubFormKind.REPEATING);
        formClass.setParentFormId(parentForm.getFormId());

        generator = new RecordGenerator(formClass)
                .parentForm(parentForm);
    }

    @Override
    public ResourceId getFormId() {
        return FORM_ID;
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public List<FormInstance> getRecords() {
        if (records == null) {
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public Supplier<FormInstance> getGenerator() {
        return generator;
    }
}
