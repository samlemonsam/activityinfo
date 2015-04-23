package org.activityinfo.ui.client.page.instance;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Provider;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.formdesigner.FormSavedGuard;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.pageView.formClass.DesignTab;
import org.activityinfo.ui.client.pageView.formClass.TableTab;
import org.activityinfo.ui.client.style.Icons;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.loading.PageLoadingPanel;

/**
 * Adapter that hosts a view of a given instance.
 */
public class InstancePage implements Page {

    public static final PageId DESIGN_PAGE_ID = new PageId("idesign");
    public static final PageId TABLE_PAGE_ID = new PageId("itable");

    // scrollpanel.bs > div.container > loadingPanel
    private final ScrollPanel scrollPanel;
    private final SimplePanel container;
    private final LoadingPanel<FormInstance> loadingPanel;

    private final PageId pageId;
    private final ResourceLocator locator;
    private final EventBus eventBus;

    public InstancePage(ResourceLocator resourceLocator, PageId pageId, EventBus eventBus) {
        this.locator = resourceLocator;
        this.pageId = pageId;
        this.eventBus = eventBus;

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
        final InstancePlace instancePlace = (InstancePlace) place;

        if (instancePlace.getPageId() == InstancePage.DESIGN_PAGE_ID) {
            loadingPanel.setDisplayWidget(new DesignTab(locator));
        } else if (instancePlace.getPageId() == InstancePage.TABLE_PAGE_ID) {
            loadingPanel.setDisplayWidget(new TableTab(locator));
        } else {
            throw new UnsupportedOperationException("Unknown page id:" + instancePlace.getPageId());
        }
        this.loadingPanel.show(new Provider<Promise<FormInstance>>() {
            @Override
            public Promise<FormInstance> get() {
                return locator.getFormInstance(instancePlace.getInstanceId());
            }
        });
        return true;
    }

    @Override
    public void shutdown() {
    }
}
