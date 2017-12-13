package chdc.frontend.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

public interface ChdcTemplates extends XTemplates {

    ChdcTemplates TEMPLATES = GWT.create(ChdcTemplates.class);

    @XTemplate(source = "Banner.html")
    SafeHtml getBannerTemplate();

    @XTemplate(source = "Sidebar.html")
    SafeHtml getSidebar(ChdcResources.Style style);


}
