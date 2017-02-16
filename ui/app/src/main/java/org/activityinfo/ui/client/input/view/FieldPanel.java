package org.activityinfo.ui.client.input.view;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.DateCell;
import com.sencha.gxt.core.client.dom.XDOM;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FieldPanel extends Widget {

    private final FormInputModel formModel;

    private Map<String, FieldContext> cells = new HashMap<>();

    public FieldPanel(FormTree formTree) {
        setElement((Element) XDOM.create(renderForm(formTree)));

        InputResources.INSTANCE.style().ensureInjected();
        formModel = new FormInputModel(formTree);

        getElement().addClassName(InputResources.INSTANCE.style().form());
        getElement().setInnerSafeHtml(renderForm(formTree));

        Set<String> consumedEvents = new HashSet<>();
        for (FieldContext fieldContext : cells.values()) {
            consumedEvents.addAll(fieldContext.getCell().getConsumedEvents());
        }
        sinkEvents(this, consumedEvents);
    }

    public native static void sinkEvents(Widget widget, Set<String> typeNames)/*-{
        var c = @com.google.gwt.user.cellview.client.CellBasedWidgetImpl::get()();
        c.@com.google.gwt.user.cellview.client.CellBasedWidgetImpl::sinkEvents(Lcom/google/gwt/user/client/ui/Widget;Ljava/util/Set;)(widget, typeNames);
    }-*/;


    private SafeHtml renderForm(FormTree tree) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();

        builder.appendHtmlConstant("<div class=\"" + InputResources.INSTANCE.style().form() + "\">");

        for (FormTree.Node node : tree.getRootFields()) {

            builder.appendHtmlConstant("<div class=\"" + InputResources.INSTANCE.style().field() + "\">");

            builder.appendHtmlConstant("<div class=\"" + InputResources.INSTANCE.style().fieldLabel() + "\">");
            builder.appendEscaped(node.getField().getLabel());
            builder.appendHtmlConstant("</div>");

            builder.appendHtmlConstant("<div data-field-id=\"" + node.getFieldId().asString() + "\">");

            Cell<?> cell = createCell(node);
            if(cell != null) {
                FieldContext context = new FieldContext(node, cell);
                context.getCell().render(context, null, builder);
                cells.put(node.getFieldId().asString(), context);
            }
            builder.appendHtmlConstant("</div>");


            builder.appendHtmlConstant("</div>");
        }

        builder.appendHtmlConstant("</div>");

        return builder.toSafeHtml();
    }

    private Cell<?> createCell(FormTree.Node node) {
        if(node.getType() instanceof TextType) {
            return new com.sencha.gxt.cell.core.client.form.TextInputCell();
        } else if(node.getType() instanceof LocalDateType) {
            DateCell dateCell = new DateCell();
            dateCell.setHideTrigger(false);
            return dateCell;
        }
        return null;
    }

    @Override
    public void onBrowserEvent(Event event) {
        final Element target = event.getEventTarget().cast();
        Element cellParent = findCellParent(target);
        if(cellParent != null) {
            FieldContext context = cells.get(cellParent.getAttribute("data-field-id"));
            if(context != null) {
                if (context.getCell().getConsumedEvents().contains(event.getType())) {
                    context.getCell().onBrowserEvent(context, cellParent, null, event, null);
                }
            }
        }
    }

    private Element findCellParent(Element target) {

        while(true) {
            if (target == null) {
                return null;
            }
            String fieldId = target.getAttribute("data-field-id");
            if (!Strings.isNullOrEmpty(fieldId)) {
                return target;
            }
            target = target.getParentElement();
        }
    }
}
