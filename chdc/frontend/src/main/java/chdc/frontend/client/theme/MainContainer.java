package chdc.frontend.client.theme;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.sencha.gxt.widget.core.client.container.Container;

public class MainContainer extends Container {

    public MainContainer() {
        Element main = Document.get().createElement("main");
        main.setAttribute("role", "main");
        setElement(main);
    }
}
