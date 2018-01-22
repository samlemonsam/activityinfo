package chdc.frontend.client.entry;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.cell.core.client.form.CheckBoxCell;
import com.sencha.gxt.cell.core.client.form.RadioCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.InsertContainer;
import org.activityinfo.ui.client.input.view.field.RadioGroupWidget;

public class RadioAppearance implements RadioGroupWidget.Appearance {


    private final String label;

    public RadioAppearance(String label) {
        this.label = label;
    }

    @Override
    public InsertContainer createContainer() {
        FlowLayoutContainer container = new FlowLayoutContainer();
        container.addStyleName("radiogroup");

        SafeHtmlBuilder headerHtml = new SafeHtmlBuilder();
        headerHtml.appendHtmlConstant("<div>");
        headerHtml.appendEscaped(label);
        headerHtml.appendHtmlConstant("</div>");

        HTML header = new HTML(headerHtml.toSafeHtml());
        header.addStyleName("radiogroup_header");
        header.addStyleName("label");
        container.add(header);

        return container;
    }

    @Override
    public RadioCell.RadioAppearance getRadioAppearance() {
        return new RadioCell.RadioAppearance() {
            @Override
            public void render(SafeHtmlBuilder sb, Boolean value, CheckBoxCell.CheckBoxCellOptions opts) {

                String checked;
                if(value == Boolean.TRUE) {
                    checked = " checked";
                } else {
                    checked = "";
                }

                sb.appendHtmlConstant("<label>");
                sb.appendHtmlConstant("<input type=\"radio\"" + checked + ">");
                sb.appendHtmlConstant("<span>");
                sb.append(opts.getBoxLabel());
                sb.appendHtmlConstant("</span>");
                sb.appendHtmlConstant("</label>");
            }


            @Override
            public XElement getInputElement(Element parent) {
                return parent.getFirstChildElement().getFirstChildElement().cast();
            }

            @Override
            public void setBoxLabel(SafeHtml boxLabel, XElement parent) {
                SpanElement span = parent.getFirstChildElement().getChild(1).cast();
                span.setInnerSafeHtml(boxLabel);
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
        };
    }
}
