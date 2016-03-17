package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.promise.Promise;

/**
 * Test Form Classes
 */
class Forms implements FormClassProvider {
    final FormClass student;
    final FormClass school;
    final FormClass village;
    final FormClassProvider formClassProvider;

    public Forms() {
        village = form("village");
        village.addElement(textField("name"));
        village.addElement(textField("code"));

        school = form("school");
        school.addElement(textField("name"));
        school.addElement(textField("schoolId"));
        school.addElement(referenceField("village", village));

        student = form("student");
        student.addElement(textField("name"));
        student.addElement(textField("studentId"));
        student.addElement(referenceField("school", school));

        formClassProvider = FormClassProviders.of(village, school, student);
    }

    private FormClass form(String name) {
        return new FormClass(ResourceId.valueOf(name))
                .setLabel(name)
                .setOwnerId(ResourceId.ROOT_ID);
    }

    private FormField textField(String label) {
        return new FormField(ResourceId.valueOf(label))
                .setType(TextType.INSTANCE)
                .setLabel(label);
    }

    private FormField referenceField(String label, FormClass formClass) {
        return  new FormField(ResourceId.valueOf(label))
                .setType(ReferenceType.single(formClass.getId()))
                .setLabel(label);
    }

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        return formClassProvider.getFormClass(resourceId);
    }

    public FormClass getStudent() {
        return student;
    }

    public FormClass getSchool() {
        return school;
    }

    public FormClass getVillage() {
        return village;
    }
    
    public AsyncFormClassProvider async() {
        return new AsyncFormClassProvider() {
            @Override
            public Promise<FormClass> getFormClass(ResourceId formClassId) {
                return Promise.resolved(formClassProvider.getFormClass(formClassId));
            }
        };
    }
}
