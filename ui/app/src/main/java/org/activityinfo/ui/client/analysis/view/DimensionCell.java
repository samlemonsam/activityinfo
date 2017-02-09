package org.activityinfo.ui.client.analysis.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.ui.client.analysis.model.DimensionModel;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;


public class DimensionCell extends AbstractCell<DimensionModel> {


    private DimensionHandler handler;

    public DimensionCell(DimensionHandler handler) {
        super(BrowserEvents.CLICK);
        this.handler = handler;
    }

    @Override
    public void render(Context context, DimensionModel model, SafeHtmlBuilder sb) {
        sb.appendEscaped(model.getLabel());
        sb.appendHtmlConstant("<div class=\"" + AnalysisBundle.INSTANCE.getStyles().handle() + "\">");
        sb.appendHtmlConstant("&#8942;");
        sb.appendHtmlConstant("</div>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, DimensionModel value, NativeEvent event, ValueUpdater<DimensionModel> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        if (CLICK.equals(event.getType())) {
            EventTarget eventTarget = event.getEventTarget();
            if (!Element.is(eventTarget)) {
                return;
            }
            Element element = Element.as(eventTarget);
            if (parent.getFirstChildElement().isOrHasChild(element) &&
                   element.hasClassName(AnalysisBundle.INSTANCE.getStyles().handle()) ) {

                handler.showMenu(element, value);
            }
        }
    }
}
