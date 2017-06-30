package org.activityinfo.store.testing;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.subform.SubFormReferenceType;

import java.util.List;

public class IncidentForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("INCIDENT_FORM");

    public static final ResourceId PROTECTION_CODE_FIELD_ID = ResourceId.valueOf("F1");

    public static final ResourceId REFERRAL_FIELD_ID = ResourceId.valueOf("F2");

    public static final ResourceId URGENCY = ResourceId.valueOf("F3");

    private static final ResourceId HIGH = ResourceId.valueOf("U1");
    private static final ResourceId MEDIUM = ResourceId.valueOf("U2");
    private static final ResourceId LOW = ResourceId.valueOf("U3");


    public static final int ROW_COUNT = 940;


    private final BioDataForm bioDataForm;

    private final FormClass formClass;
    private final FormField codeField;
    private final FormField referralField;

    private List<FormInstance> records = null;
    private RecordGenerator generator;

    public IncidentForm(BioDataForm bioDataForm) {
        this.bioDataForm = bioDataForm;
        formClass = new FormClass(FORM_ID);
        formClass.setLabel("Incident Form");

        codeField = formClass.addField(PROTECTION_CODE_FIELD_ID)
                .setCode("PCODE")
                .setLabel("Protection Code")
                .setType(new ReferenceType(Cardinality.SINGLE, BioDataForm.FORM_ID))
                .setRequired(true)
                .setVisible(true);

        formClass.addField(URGENCY)
                .setLabel("Urgency of the case")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(HIGH, "High"),
                        new EnumItem(MEDIUM, "Medium"),
                        new EnumItem(LOW, "Low")));

        referralField = formClass.addField(REFERRAL_FIELD_ID)
                .setLabel("Referrals")
                .setType(new SubFormReferenceType(ReferralSubForm.FORM_ID));

        generator = new RecordGenerator(formClass)
                .distribution(PROTECTION_CODE_FIELD_ID, new RefGenerator(bioDataForm));

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

            this.records = generator
                    .get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public RecordGenerator getGenerator() {
        return generator;
    }

    public RecordRef getRecordRef(int i) {
        return getRecords().get(i).getRef();
    }
}
