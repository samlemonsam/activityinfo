package org.activityinfo.ui.client.page.config.design;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

import java.util.List;

public class LocationTypeListRenderer implements SafeHtmlRenderer<List<LocationTypeEntry>> {

    interface Templates extends SafeHtmlTemplates {

        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml header(String itemStyle, String label);

        @Template("<div role=\"listitem\" class=\"{0}\">{1}</div>")
        SafeHtml item(String itemStyle, String contents);

        @Template("<div role=\"listitem\" class=\"{0}\">{1} <span class=\"{2}\">{3}</div>")
        SafeHtml itemWithId(String itemStyle, String contents, String idStyle, int id);

    }


    interface ListStyles extends CssResource {
        String item();
        String header();
        String id();
    }

    interface Bundle extends ClientBundle {
        @Source("LocationTypeList.css")
        ListStyles styles();
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);
    private static final Bundle BUNDLE = GWT.create(Bundle.class);

    public LocationTypeListRenderer() {
        BUNDLE.styles().ensureInjected();
    }

    public static String getItemSelector() {
        return "." + BUNDLE.styles().item();
    }

    @Override
    public SafeHtml render(List<LocationTypeEntry> entries) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        render(entries, html);
        return html.toSafeHtml();
    }

    @Override
    public void render(List<LocationTypeEntry> entries, SafeHtmlBuilder html) {
        String currentHeader = "";
        for (LocationTypeEntry entry : entries) {
            String header = entry.getHeader();
            if(!header.equals(currentHeader)) {
                html.append(TEMPLATES.header(BUNDLE.styles().header(), header));
                currentHeader = header;
            }
            if(entry.isPublic()) {
                html.append(TEMPLATES.itemWithId(BUNDLE.styles().item(), entry.getLocationTypeName(),
                        BUNDLE.styles().id(),
                        entry.getId()));
            } else {
                html.append(TEMPLATES.item(BUNDLE.styles().item(), entry.getLocationTypeName()));
            }
        }
    }
}
