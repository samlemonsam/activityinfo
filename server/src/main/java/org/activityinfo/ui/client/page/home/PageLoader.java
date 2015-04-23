package org.activityinfo.ui.client.page.home;

import com.google.common.base.Function;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.application.FolderClass;
import org.activityinfo.core.shared.criteria.ClassCriteria;
import org.activityinfo.core.shared.criteria.CriteriaIntersection;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.ui.client.page.*;
import org.activityinfo.ui.client.page.instance.InstancePage;
import org.activityinfo.ui.client.page.instance.InstancePlace;
import org.activityinfo.ui.client.style.BaseStylesheet;

import javax.annotation.Nullable;
import java.util.List;

public class PageLoader implements org.activityinfo.ui.client.page.PageLoader {

    private final NavigationHandler pageManager;
    private ResourceLocator resourceLocator;

    @Inject
    public PageLoader(NavigationHandler pageManager,
                      PageStateSerializer placeSerializer,
                      ResourceLocator resourceLocator) {

        this.resourceLocator = resourceLocator;
        this.pageManager = pageManager;


        pageManager.registerPageLoader(HomePage.PAGE_ID, this);
        placeSerializer.registerParser(HomePage.PAGE_ID, new HomePlace.Parser());

        pageManager.registerPageLoader(InstancePage.DESIGN_PAGE_ID, this);
        placeSerializer.registerParser(InstancePage.DESIGN_PAGE_ID, new InstancePlace.Parser(InstancePage.DESIGN_PAGE_ID));

        pageManager.registerPageLoader(InstancePage.TABLE_PAGE_ID, this);
        placeSerializer.registerParser(InstancePage.TABLE_PAGE_ID, new InstancePlace.Parser(InstancePage.TABLE_PAGE_ID));
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {

        BaseStylesheet.INSTANCE.ensureInjected();

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (pageState instanceof HomePlace) {
                    loadHomePage(callback);

                } else if (pageState instanceof InstancePlace) {
                    InstancePlace instancePlace = (InstancePlace) pageState;
                    InstancePage page = new InstancePage(resourceLocator, instancePlace.getPageId(), pageManager.getEventBus());
                    page.navigate(pageState);
                    callback.onSuccess(page);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    private void loadHomePage(AsyncCallback<Page> callback) {
        CriteriaIntersection criteria = new CriteriaIntersection(new ClassCriteria(FolderClass.CLASS_ID),
                ParentCriteria.isRoot());

        resourceLocator.queryInstances(criteria).then(new Function<List<FormInstance>, Page>() {
            @Nullable @Override
            public Page apply(List<FormInstance> formInstances) {
                return new HomePage(formInstances);
            }
        }).then(callback);
    }
}
