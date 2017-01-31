package org.activityinfo.ui.client.page;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.resource.ResourcePage;
import org.activityinfo.ui.client.page.resource.ResourcePlace;
import org.activityinfo.ui.client.style.BaseStylesheet;

public class FormPageLoader implements PageLoader {

    private final NavigationHandler pageManager;
    private ResourceLocator resourceLocator;
    private final StateProvider stateProvider;

    @Inject
    public FormPageLoader(NavigationHandler pageManager,
                      PageStateSerializer placeSerializer,
                      ResourceLocator resourceLocator,
                      StateProvider stateProvider
    ) {

        this.resourceLocator = resourceLocator;
        this.pageManager = pageManager;
        this.stateProvider = stateProvider;

        pageManager.registerPageLoader(ResourcePage.DESIGN_PAGE_ID, this);
        placeSerializer.registerParser(ResourcePage.DESIGN_PAGE_ID, new ResourcePlace.Parser(ResourcePage.DESIGN_PAGE_ID));

        pageManager.registerPageLoader(ResourcePage.TABLE_PAGE_ID, this);
        placeSerializer.registerParser(ResourcePage.TABLE_PAGE_ID, new ResourcePlace.Parser(ResourcePage.TABLE_PAGE_ID));
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {

        BaseStylesheet.INSTANCE.ensureInjected();

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (pageState instanceof ResourcePlace) {
                    ResourcePlace resourcePlace = (ResourcePlace) pageState;
                    ResourcePage page = new ResourcePage(resourceLocator, resourcePlace.getPageId(), stateProvider);
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
}
