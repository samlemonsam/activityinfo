package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.List;

public class IncidentForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("INCIDENT_FORM");

    public static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");

    public static final ResourceId REFERRAL_FIELD_ID = ResourceId.valueOf("F2");

    public static final int ROW_COUNT = 940;

    private final BioDataForm bioDataForm;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField referralField;

    private List<FormInstance> records = null;

    public IncidentForm(BioDataForm bioDataForm) {
        this.bioDataForm = bioDataForm;
        formClass = new FormClass(FORM_ID);
        codeField = formClass.addField(PROTECTION_CODE_FIELD_ID)
                .setCode("PCODE")
                .setLabel("Protection Code")
                .setType(new ReferenceType(Cardinality.SINGLE, Survey.FORM_ID))
                .setRequired(true)
                .setVisible(true);

        referralField = formClass.addField(REFERRAL_FIELD_ID)
                .setLabel("Referrals")
                .setType(new SubFormReferenceType(ReferralSubForm.FORM_ID));

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
        if(records == null) {
            this.records = new RecordGenerator(formClass)
                    .distribution(PROTECTION_CODE_FIELD_ID, new RefGenerator(bioDataForm))
                    .generate(ROW_COUNT);
        }
        return records;
    }
}
