package org.activityinfo.ui.client.input.view.field;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.cell.core.client.form.FieldCell;
import com.sencha.gxt.cell.core.client.form.TwinTriggerFieldCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.theme.triton.client.base.field.Css3TriggerFieldAppearance;
import com.sencha.gxt.theme.triton.client.base.field.Css3ValueBaseFieldAppearance;
import org.activityinfo.ui.client.input.view.InputResources;

public class QuantityFieldAppearance extends Css3ValueBaseFieldAppearance implements TwinTriggerFieldCell.TwinTriggerFieldAppearance {

    private static final Css3TriggerFieldAppearance.Css3TriggerFieldResources RESOURCES =
            GWT.create(Css3TriggerFieldAppearance.Css3TriggerFieldResources.class);
    private String units;

    public QuantityFieldAppearance(String units) {
        super(RESOURCES);
        this.units = units;
    }


    @Override
    public void onResize(XElement parent, int width, int height, boolean hideTrigger) {
        if (width != -1) {
            width = Math.max(0, width);
            parent.getFirstChildElement().getStyle().setPropertyPx("width", width);
        }
    }

    @Override
    public void onTriggerClick(XElement parent, boolean click) {
        // NOOP: No trigger
    }

    @Override
    public void onTriggerOver(XElement parent, boolean over) {
        // NOOP: No trigger
    }

    @Override
    public void render(SafeHtmlBuilder sb, String value, FieldCell.FieldAppearanceOptions options) {
        int width = options.getWidth();

        if (width == -1) {
            width = 150;
        }

        SafeStylesBuilder inputStylesBuilder = new SafeStylesBuilder();
        inputStylesBuilder.appendTrustedString("width:100%;");

        sb.appendHtmlConstant("<div style='width:" + width + "px;'>");

        sb.appendHtmlConstant("<div class='" + RESOURCES.style().wrap() + "'>");
        renderInput(sb, value, inputStylesBuilder.toSafeStyles(), options);
        sb.appendHtmlConstant("<span class='" + InputResources.INSTANCE.style().fieldUnits() + "'>");
        sb.appendEscaped(units);
        sb.appendHtmlConstant("</span>");
        sb.appendHtmlConstant("</div></div>");

    }


    protected void renderInput(SafeHtmlBuilder shb, String value, SafeStyles inputStyles, FieldCell.FieldAppearanceOptions options) {
        StringBuilder sb = new StringBuilder();
        sb.append("<input ");

        if (options.isDisabled()) {
            sb.append("disabled=true ");
        }

        if (options.getName() != null) {
            sb.append("name='").append(SafeHtmlUtils.htmlEscape(options.getName())).append("' ");
        }

        if (options.isReadonly() || !options.isEditable()) {
            sb.append("readonly ");
        }

        if (inputStyles != null) {
            sb.append("style='").append(inputStyles.asString()).append("' ");
        }

        sb.append("class='").append(RESOURCES.style().field()).append(" ").append(RESOURCES.style().text());

        if (!options.isEditable()) {
            sb.append(" ").append(RESOURCES.style().noedit());
        }

        sb.append("' ");

        sb.append("type='text' value='").append(SafeHtmlUtils.htmlEscape(value)).append("' ");

        sb.append("/>");

        shb.append(SafeHtmlUtils.fromTrustedString(sb.toString()));
    }

    public void setEditable(XElement parent, boolean editable) {
        getInputElement(parent).setClassName(RESOURCES.style().noedit(), !editable);
    }


    @Override
    public boolean triggerIsOrHasChild(XElement parent, Element target) {
        return false;
    }

    @Override
    public XElement getInputElement(Element parent) {
        return parent.<XElement>cast().selectNode("input");
    }

    @Override
    public boolean twinTriggerIsOrHasChild(XElement parent, Element target) {
        return false;
    }

    @Override
    public void onTwinTriggerOver(XElement parent, boolean over) {
    }

    @Override
    public void onTwinTriggerClick(XElement parent, boolean click) {
    }
}
