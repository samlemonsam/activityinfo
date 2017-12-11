package chdc.frontend.client.theme;

import chdc.frontend.client.i18n.ChdcLabels;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.FieldCell;
import com.sencha.gxt.cell.core.client.form.TextInputCell;
import com.sencha.gxt.core.client.dom.XElement;

public class SearchFieldAppearance implements TextInputCell.TextFieldAppearance {


    interface Templates extends SafeHtmlTemplates {
        @Template("<input type=\"search\" placeholder=\"{0}\" aria-label=\"{0}\" value=\"{1}\">")
        SafeHtml input(String label, String value);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);


    @Override
    public void render(SafeHtmlBuilder sb, String type, String value, FieldCell.FieldAppearanceOptions options) {
        sb.append(TEMPLATES.input(ChdcLabels.LABELS.search(), value));
    }

    @Override
    public void onResize(XElement parent, int width, int height) {
    }

    @Override
    public XElement getInputElement(Element parent) {
        XElement input = parent.getFirstChildElement().cast();
        assert input.getTagName().equalsIgnoreCase("INPUT");
        return input;
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
