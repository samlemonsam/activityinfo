package org.activityinfo.ui.client.analysis.view;

import com.google.common.base.Function;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;


public class PillCell<T> extends AbstractCell<T> {


    private Function<T, String> labelFunction;
    private PillHandler<T> handler;

    public PillCell(Function<T, String> labelFunction, PillHandler<T> handler) {
        super(BrowserEvents.CLICK);
        this.labelFunction = labelFunction;
        this.handler = handler;
    }

    @Override
    public void render(Context context, T model, SafeHtmlBuilder sb) {
        sb.appendEscaped(labelFunction.apply(model));
        sb.appendHtmlConstant("<div class=\"" + AnalysisBundle.INSTANCE.getStyles().handle() + "\">");
        sb.appendHtmlConstant("&#8942;");
        sb.appendHtmlConstant("</div>");
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, T value, NativeEvent event, ValueUpdater<T> valueUpdater) {
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
