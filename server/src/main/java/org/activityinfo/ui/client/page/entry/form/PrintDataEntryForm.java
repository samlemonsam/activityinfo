package org.activityinfo.ui.client.page.entry.form;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.monitor.MaskingAsyncMonitor;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.entry.form.resources.SiteFormResources;

public class PrintDataEntryForm extends Window {

    private StringBuilder html;
    private Frame frame;
    private Dispatcher dispatcher;

    public PrintDataEntryForm(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        setHeadingText(I18N.CONSTANTS.preview());
        setWidth(450);
        setHeight(250);

        frame = new Frame();
        frame.getElement().setPropertyInt("frameBorder", 0);
        frame.setSize("100%", "100%");
        setLayout(new FlowLayout());
        add(frame);

        getButtonBar().add(new Button(I18N.CONSTANTS.printForm(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                doPrint(getFrameElement());
            }
        }));
        getButtonBar().add(new Button(I18N.CONSTANTS.close(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                PrintDataEntryForm.this.hide();
            }
        }));

    }

    public void print(final int activityId) {
        setVisible(true);
        dispatcher.execute(new GetActivityForm(activityId),
                new MaskingAsyncMonitor(this, I18N.CONSTANTS.loading()),
                new AsyncCallback<ActivityFormDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        showError(caught);
                    }

                    @Override
                    public void onSuccess(ActivityFormDTO result) {
                        renderForm(result);
                    }

                });
    }

    private void renderForm(ActivityFormDTO activity) {
        try {
            String html = render(activity);

            getFrameElement().getStyle().setBackgroundColor("white");

            fillIframe(getFrameElement(), html);
        } catch (Exception e) {
            showError(e);
        }
    }

    private final native void fillIframe(IFrameElement iframe, String content) /*-{
      var doc = iframe.document;

      if (iframe.contentDocument) {
        doc = iframe.contentDocument; // For NS6
      } else if (iframe.contentWindow) {
        doc = iframe.contentWindow.document; // For IE5.5 and IE6
      }

      // Put the content in the iframe
      doc.open();
      doc.writeln(content);
      doc.close();
    }-*/;

    private void showError(Throwable e) {
        MessageBox.alert(I18N.CONSTANTS.error(),
                "There was an error printing the data entry form: " + e.getMessage(),
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        PrintDataEntryForm.this.hide();
                    }
                });
    }

    private String render(ActivityFormDTO activity) {

        String contents = getFormContents();

        contents = contents.replace("{$activityName}", activity.getName())
                           .replace("{$databaseName}", activity.getDatabaseName())
                           .replace("{$activityName}", activity.getName())
                           .replace("{$projectName}", addProjects(activity))
                           .replace("{$indicators}", addIndicators(activity))
                           .replace("{$attributes}", addAttributes(activity));

        html = new StringBuilder();
        html.append(contents);
        return html.toString();
    }

    private String addProjects(ActivityFormDTO activity) {
        String result = "";
        for (ProjectDTO projectDTO : activity.getProjects()) {
            result += projectDTO.getName() + ",";
        }
        return result;
    }

    private String getFormContents() {
        TextResource formPage = SiteFormResources.INSTANCE.collectionForm();
        return formPage.getText();
    }

    private void schedulePrint() {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                doPrint(getFrameElement());
            }
        });

    }

    private static native void doPrint(IFrameElement frame) /*-{
      var contentWindow = frame.contentWindow;
      contentWindow.focus();
      contentWindow.print();
    }-*/;


    private String addIndicators(ActivityFormDTO activity) {
        StringBuilder builder = new StringBuilder();

        builder.append("<table border=\"1px\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\" " +
                       "class=\"form-detail\">");

        for (IndicatorGroup group : activity.groupIndicators()) {

            if (group.getName() != null) {
                builder.append("<tr><td colspan='3'><h3 class='indicatorGroup'> " + group.getName() + "</h3><td></tr>");
            }

            builder.append("<tr>");
            builder.append("<td>Indicator</td>");
            builder.append("<td>Valeur</td>");
            builder.append("<td>Units</td>");
            builder.append("</tr>");
            for (IndicatorDTO indicator : group.getIndicators()) {
                addIndicator(indicator, builder);
            }

        }

        builder.append("</table>");

        return builder.toString();
    }

    private void addIndicator(IndicatorDTO indicator, StringBuilder builder) {
        builder.append("<tr>");
        builder.append("<td>" + indicator.getName() + "</td>");
        builder.append("<td>&nbsp;</td>");
        builder.append("<td>" + indicator.getUnits() + "</td>");
        builder.append("</tr>");
    }

    private String addAttributes(ActivityFormDTO activity) {

        StringBuilder builder = new StringBuilder();
        for (AttributeGroupDTO attributeGroup : activity.getAttributeGroups()) {

            builder.append("<tr>");
            builder.append("<td id=\"field-set\" valign=\"top\">" + attributeGroup.getName() + ":</td><td>");

            attributeCheckBoxGroup(attributeGroup, builder);
            builder.append("</td></tr>");
        }
        return builder.toString();
    }

    private void attributeCheckBoxGroup(AttributeGroupDTO group, StringBuilder builder) {

        for (AttributeDTO attribture : group.getAttributes()) {
            builder.append("[  ] " + attribture.getName() + "<br />");
        }

    }

    private IFrameElement getFrameElement() {
        return frame.getElement().cast();
    }

}
