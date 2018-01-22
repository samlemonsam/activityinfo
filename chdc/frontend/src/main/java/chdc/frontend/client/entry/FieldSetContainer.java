package chdc.frontend.client.entry;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.InsertContainer;

public class FieldSetContainer extends InsertContainer {


    private static class Legend extends Widget {

        public Legend(String labelText) {
            Element label = Document.get().createLegendElement();
            label.setInnerText(labelText);
            setElement(label);
        }
    }


    public FieldSetContainer(String labelText) {
        setElement(Document.get().createFieldSetElement());
        add(new Legend(labelText));
    }


    public boolean isEmpty() {
        return getChildren().size() == 0;
    }
}
