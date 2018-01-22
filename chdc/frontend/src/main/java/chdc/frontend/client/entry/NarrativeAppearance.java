package chdc.frontend.client.entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.cell.core.client.form.FieldCell;
import com.sencha.gxt.cell.core.client.form.TextAreaInputCell;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.Size;


/**
 * Defines the appearance for a narrative input cell.
 */
public class NarrativeAppearance implements TextAreaInputCell.TextAreaAppearance {

    private final String label;

    public NarrativeAppearance(String label) {
        this.label = label;
    }

    @Override
    public void render(SafeHtmlBuilder sb, String value, FieldCell.FieldAppearanceOptions options) {

        //<label>
        //	<div>Narrative</div>
        //	<textarea placeholder="">CF conducted an airstrike targeting IS positions.</textarea>
        //</label>

        sb.appendHtmlConstant("<label>");
        sb.appendHtmlConstant("<div>");
        sb.appendEscaped(label);
        sb.appendHtmlConstant("</div>");
        sb.appendHtmlConstant("<textarea>");
        sb.appendEscaped(value);
        sb.appendHtmlConstant("</textarea>");
        sb.appendHtmlConstant("</label>");
    }

    @Override
    public XElement getInputElement(Element parent) {
        Element label = parent.getFirstChildElement();
        Element textarea = label.getChild(1).cast();
        assert textarea.getTagName().equalsIgnoreCase("textarea");
        return textarea.cast();
    }

    @Override
    public void onResize(XElement parent, int width, int height) {
    }


    @Override
    public void onEmpty(Element parent, boolean empty) {
    }

    @Override
    public void onFocus(Element parent, boolean focus) {
    }

    @Override
    public void onValid(Element parent, boolean valid) {
    }

    @Override
    public void setReadOnly(Element parent, boolean readonly) {
    }
}
