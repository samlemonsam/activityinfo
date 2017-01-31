package org.activityinfo.ui.client.page.common;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

import java.util.List;

/**
 * Renders gallery of icons with descriptions
 */
public class GalleryRenderer<T extends ModelData> implements SafeHtmlRenderer<List<T>> {


    private String iconPath;

    public GalleryRenderer(String iconPath) {
        this.iconPath = iconPath;
    }

    interface Templates extends SafeHtmlTemplates {

        @Template("<dd><img src=\"{0}\" title=\"{1}\"><div><h4>{1}</h4><p>{2}</p></div></dd>")
        SafeHtml item(SafeUri iconUri, String name, String description);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);


    @Override
    public SafeHtml render(List<T> models) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        render(models, builder);
        return builder.toSafeHtml();
    }

    @Override
    public void render(List<T> models, SafeHtmlBuilder html) {
        html.appendHtmlConstant("<dl>");

        for (T model : models) {
            SafeUri iconPath = UriUtils.fromTrustedString(GWT.getModuleBaseURL() + this.iconPath + model.get("path"));
            String name = model.get("name");
            String description = model.get("description");

            html.append(TEMPLATES.item(iconPath, name, description));
        }

        html.appendHtmlConstant("<div style=\"clear:left;\"></div>");
        html.appendHtmlConstant("</dl>");
    }
}
