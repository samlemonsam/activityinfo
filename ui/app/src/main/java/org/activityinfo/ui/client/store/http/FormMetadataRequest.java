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
package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.form.FormMetadata;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

/**
 * Requests a form's metadata, including its schema.
 *
 * <p>In nearly all cases, we will not only need an individual form, but all of its related forms. For this
 * reason, we will prefetch the entire tree in anticipation of follow up requests. The {@link FormRequestPrecacher}
 * is used to pre-populate the HttpStore's form cache.</p>
 */
public class FormMetadataRequest implements HttpRequest<FormMetadata> {

    private ResourceId formId;
    private final FormRequestPrecacher precacher;

    private boolean initialRequest = true;

    public FormMetadataRequest(ResourceId formId, FormRequestPrecacher precacher) {
        this.formId = formId;
        this.precacher = precacher;
    }

    @Override
    public Promise<FormMetadata> execute(ActivityInfoClientAsync async) {
        if(initialRequest) {
            return prefetchTree(async);
        }
        return async.getFormMetadata(formId.asString());
    }

    private Promise<FormMetadata> prefetchTree(ActivityInfoClientAsync async) {
        initialRequest = false;
        return async.getFormTreeList(formId).then(list -> {
            FormMetadata root = null;
            for (FormMetadata form : list) {
                if(form.getId().equals(this.formId)) {
                    root = form;
                } else {
                    precacher.precache(form);
                }
            }
            if(root == null) {
                return FormMetadata.notFound(formId);
            } else {
                return root;
            }
        });
    }


    @Override
    public int refreshInterval(FormMetadata result) {
        return -1;
    }
}
