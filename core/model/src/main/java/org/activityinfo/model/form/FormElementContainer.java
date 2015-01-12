package org.activityinfo.model.form;


import org.activityinfo.model.resource.ResourceId;

import java.util.List;

public interface FormElementContainer {

    ResourceId getId();

    List<FormElement> getElements();

    FormElementContainer addElement(FormElement element);

    FormElementContainer insertElement(int index, FormElement element);
}
