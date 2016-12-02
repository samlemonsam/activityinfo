package org.activityinfo.ui.client.page.resource;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Provider;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormSavedGuard;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.pageView.formClass.DesignTab;
import org.activityinfo.ui.client.pageView.formClass.TableTab;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.loading.PageLoadingPanel;
import org.activityinfo.ui.icons.Icons;

/**
 * Adapter that hosts a view of a given instance.
 */
public class ResourcePage implements Page {

    public static final PageId DESIGN_PAGE_ID = new PageId("idesign");
    public static final PageId TABLE_PAGE_ID = new PageId("itable");

    // scrollpanel.bs > div.container > loadingPanel
    private final ScrollPanel scrollPanel;
    private final SimplePanel container;
    private final LoadingPanel<ResourceId> loadingPanel;

    private final PageId pageId;
    private final ResourceLocator locator;
    private final StateProvider stateProvider;

    public ResourcePage(ResourceLocator resourceLocator, PageId pageId, StateProvider stateProvider) {
        this.locator = resourceLocator;
        this.pageId = pageId;
        this.stateProvider = stateProvider;

        Icons.INSTANCE.ensureInjected();

        this.loadingPanel = new LoadingPanel<>(new PageLoadingPanel());

        this.container = new SimplePanel(loadingPanel.asWidget());
        this.container.addStyleName("container");

        this.scrollPanel = new ScrollPanel(container);
        this.scrollPanel.addStyleName("bs");
    }

    @Override
    public PageId getPageId() {
        return pageId;
    }

    @Override
    public Object getWidget() {
        return scrollPanel;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        if (!FormSavedGuard.callNavigationCallback(scrollPanel, callback)) {
            callback.onDecided(true);
        }
    }

    @Override
    public String beforeWindowCloses() {
        FormSavedGuard guard = FormSavedGuard.getGuard(scrollPanel);
        if (guard == null || guard.isSaved()) {
            return null;
        } else {
            return I18N.CONSTANTS.unsavedChangesWarning();
        }
    }

    @Override
    public boolean navigate(PageState place) {
        final ResourcePlace resourcePlace = (ResourcePlace) place;

        if (resourcePlace.getPageId() == ResourcePage.DESIGN_PAGE_ID) {
            loadingPanel.setDisplayWidget(new DesignTab(locator, stateProvider));
        } else if (resourcePlace.getPageId() == ResourcePage.TABLE_PAGE_ID) {
            loadingPanel.setDisplayWidget(new TableTab(locator, stateProvider));
        } else {
            throw new UnsupportedOperationException("Unknown page id:" + resourcePlace.getPageId());
        }
        this.loadingPanel.show(new Provider<Promise<ResourceId>>() {
            @Override
            public Promise<ResourceId> get() {
                return Promise.resolved(resourcePlace.getInstanceId());
            }
        });
        return true;
    }

    @Override
    public void shutdown() {
    }
}
