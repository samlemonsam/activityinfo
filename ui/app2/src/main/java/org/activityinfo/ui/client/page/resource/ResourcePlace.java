/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.page.resource;

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
public class ResourcePlace implements PageState {

    private ResourceId instanceId;
    private PageId pageId;

    public ResourcePlace(ResourceId resourceId, PageId part) {
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
        public ResourcePlace parse(String token) {
            return new ResourcePlace(ResourceId.valueOf(token), pageId);
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
