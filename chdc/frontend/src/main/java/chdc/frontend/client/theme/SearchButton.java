package chdc.frontend.client.theme;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.ButtonCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.button.CellButtonBase;

public class SearchButton extends CellButtonBase<String> {

    public static class Appearance implements ButtonCell.ButtonCellAppearance<String> {

        @Override
        public void render(ButtonCell<String> cell, Cell.Context context, String value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<button class=\"button\">");
            sb.append(Icon.SEARCH.svg());
            sb.appendHtmlConstant("</button>");
        }

        @Override
        public XElement getButtonElement(XElement parent) {
            Element button = parent.getFirstChildElement();
            assert button.getTagName().equalsIgnoreCase("BUTTON");
            return button.cast();
        }

        @Override
        public XElement getFocusElement(XElement parent) {
            return getButtonElement(parent);
        }

        @Override
        public void onFocus(XElement parent, boolean focused) {
        }

        @Override
        public void onOver(XElement parent, boolean over) {
        }

        @Override
        public void onPress(XElement parent, boolean pressed) {
        }

        @Override
        public void onToggle(XElement parent, boolean pressed) {
        }
    }

    public SearchButton() {
        super(new ButtonCell<String>(new Appearance()));
    }
}
