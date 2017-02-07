package org.activityinfo.ui.client.measureDialog.model;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;

import java.util.Collections;
import java.util.List;

/**
 * Set of selected forms
 */
public class FormSet {


    private List<FormClass> formSchemas;

    public FormSet(List<FormClass> formSchemas) {

        this.formSchemas = formSchemas;
    }

    public int size() {
        return formSchemas.size();
    }

    public List<FormField> getCommonFields() {
        if(formSchemas.isEmpty()) {
            return Collections.emptyList();
        } else {
            return formSchemas.get(0).getFields();
        }
    }

    public boolean isEmpty() {
        return formSchemas.isEmpty();
    }

    public FormClass getFirst() {
        return formSchemas.get(0);
    }
}
