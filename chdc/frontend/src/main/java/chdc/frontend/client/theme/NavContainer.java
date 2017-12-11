package chdc.frontend.client.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.sencha.gxt.widget.core.client.container.InsertContainer;

public class NavContainer extends InsertContainer {

    public NavContainer() {
        Element main = Document.get().createElement("nav");
        main.setAttribute("role", "navigation");
        setElement(main);
    }
}
