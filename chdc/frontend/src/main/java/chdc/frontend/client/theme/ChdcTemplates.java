package chdc.frontend.client.theme;

import chdc.frontend.client.i18n.ChdcLabels;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

public interface ChdcTemplates extends XTemplates {

    ChdcTemplates TEMPLATES = GWT.create(ChdcTemplates.class);

    @XTemplate(source = "Banner.html")
    SafeHtml banner();

    @XTemplate(source = "NavigationHeader.html")
    SafeHtml sidebarHeader();

    @XTemplate(source = "QuickSearchForm.html")
    SafeHtml quickSearchForm(ChdcLabels labels);

}
