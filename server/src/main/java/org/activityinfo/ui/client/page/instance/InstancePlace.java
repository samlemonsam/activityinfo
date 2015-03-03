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

    public static final String DEFAULT_VIEW = "table";

    private ResourceId instanceId;
    private String view = DEFAULT_VIEW;

    public InstancePlace(ResourceId instanceId) {
        this.instanceId = instanceId;
    }

    public InstancePlace(ResourceId resourceId, String part) {
        this.instanceId = resourceId;
        this.view = part;
    }

    @Override
    public String serializeAsHistoryToken() {
        StringBuilder token = new StringBuilder(instanceId.asString());
        if(view != null) {
            token.append("/").append(view);
        }
        return token.toString();
    }

    @Override
    public PageId getPageId() {
        return InstancePage.PAGE_ID;
    }

    public ResourceId getInstanceId() {
        return instanceId;
    }

    @Override
    public List<PageId> getEnclosingFrames() {
        return Lists.newArrayList(InstancePage.PAGE_ID);
    }

    @Override
    public Section getSection() {
        return null;
    }

    public String getView() {
        return view;
    }



    public static class Parser implements PageStateParser {

        @Override
        public InstancePlace parse(String token) {
            String parts[] = token.split("/");
            if(parts.length == 1) {
                return new InstancePlace(ResourceId.create(parts[0]));
            } else {
                return new InstancePlace(ResourceId.create(parts[0]), parts[1]);
            }
        }
    }


    public static SafeUri safeUri(ResourceId instanceId) {
        return UriUtils.fromTrustedString("#" + historyToken(instanceId));
    }

    public static SafeUri safeUri(ResourceId id, String tab) {
        return UriUtils.fromTrustedString("#" + historyToken(id, tab));
    }

    public static String historyToken(ResourceId instanceId) {
        return "i/" + instanceId.asString();
    }

    public static String historyToken(ResourceId instanceId, String tab) {
        return historyToken(instanceId) + "/" + tab;
    }
}
