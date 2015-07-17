package org.activityinfo.ui.client.page.instance;

import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.PageStateParser;
import org.activityinfo.ui.client.page.app.Section;

import java.util.List;

/**
 * Place corresponding to the view of a instance.
 */
public class InstancePlace implements PageState {

    private ResourceId instanceId;
    private PageId pageId;

    public InstancePlace(ResourceId resourceId, PageId part) {
        this.instanceId = resourceId;
        this.pageId = part;
    }

    @Override
    public String serializeAsHistoryToken() {
        return instanceId.asString();
    }

    @Override
    public PageId getPageId() {
        return pageId;
    }

    public ResourceId getInstanceId() {
        return instanceId;
    }

    @Override
    public List<PageId> getEnclosingFrames() {
        return Lists.newArrayList(pageId);
    }

    @Override
    public Section getSection() {
        return null;
    }

    public static class Parser implements PageStateParser {

        private PageId pageId;

        public Parser(PageId pageId) {
            this.pageId = pageId;
        }

        @Override
        public InstancePlace parse(String token) {
            return new InstancePlace(ResourceId.valueOf(token), pageId);
        }
    }

    public static SafeUri safeUri(ResourceId instanceId) {
        return UriUtils.fromTrustedString("#" + historyToken(instanceId));
    }

    public static SafeUri safeUri(ResourceId id, PageId pageId) {
        return UriUtils.fromTrustedString("#" + pageId + "/" + id.asString());
    }

    public static String historyToken(ResourceId instanceId) {
        return "i/" + instanceId.asString();
    }

}
