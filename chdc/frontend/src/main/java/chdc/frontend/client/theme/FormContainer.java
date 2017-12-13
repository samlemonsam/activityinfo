package chdc.frontend.client.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.sencha.gxt.widget.core.client.container.InsertContainer;

public class FormContainer extends InsertContainer {

    public FormContainer(String className) {
        Element form = Document.get().createFormElement();
        form.setClassName(className);
        setElement(form);
    }
}
