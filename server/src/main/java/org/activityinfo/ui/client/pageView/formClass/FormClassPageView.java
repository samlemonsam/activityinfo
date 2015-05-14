package org.activityinfo.ui.client.pageView.formClass;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.form.FormInstance;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.chrome.PageHeader;
import org.activityinfo.ui.client.page.instance.InstancePlace;
import org.activityinfo.ui.client.pageView.InstancePageView;
import org.activityinfo.ui.client.pageView.InstanceViewModel;
import org.activityinfo.ui.client.style.Icons;
import org.activityinfo.ui.client.widget.DisplayWidget;

import java.util.Map;

/**
 * Provides a view for a FormClass instance
 */
public class FormClassPageView implements InstancePageView {

    private final Widget rootElement;

    interface FormViewUiBinder extends UiBinder<HTMLPanel, FormClassPageView> {
    }

    private static FormViewUiBinder ourUiBinder = GWT.create(FormViewUiBinder.class);

    @UiField PageHeader pageHeader;

    @UiField(provided = true)
    Widget tabWidget;

    DisplayWidget<FormInstance> tabView;

    @UiField AnchorElement designTab;
    @UiField AnchorElement tableTab;
    @UiField
    LIElement designTabContainer;
    @UiField
    LIElement tableTabContainer;

    private Map<String, AnchorElement> tabs = Maps.newHashMap();

    public FormClassPageView(DisplayWidget<FormInstance> tabView) {
        this.tabView = tabView;
        this.tabWidget = tabView.asWidget();

        rootElement = ourUiBinder.createAndBindUi(this);

        tabs.put("table", tableTab);
        tabs.put("design", designTab);

        Icons.INSTANCE.ensureInjected();
    }

    @Override
    public Promise<Void> show(InstanceViewModel view) {

        pageHeader.setPageTitle(view.getInstance().getString(FormClass.LABEL_FIELD_ID));
        pageHeader.setIconStyle("fa fa-edit");

        for(String tab : tabs.keySet()) {
            tabs.get(tab).setHref(InstancePlace.safeUri(view.getInstance().getId(), tab));
        }

        setTabSelected(view.getPath());

        return tabView.show(view.getInstance());
    }

    private void setTabSelected(String path) {
        designTabContainer.removeClassName("active");
        tableTabContainer.removeClassName("active");
        if (path.equals("design")) {
            designTabContainer.addClassName("active");
        } else {
            tableTabContainer.addClassName("active");
        }
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

}