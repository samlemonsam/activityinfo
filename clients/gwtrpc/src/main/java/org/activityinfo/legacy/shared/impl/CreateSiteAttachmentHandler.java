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
package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.query.SqlInsert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.CreateSiteAttachment;
import org.activityinfo.legacy.shared.command.result.VoidResult;

public class CreateSiteAttachmentHandler implements CommandHandlerAsync<CreateSiteAttachment, VoidResult> {

    @Override
    public void execute(CreateSiteAttachment command, ExecutionContext context, AsyncCallback<VoidResult> callback) {

        SqlInsert.insertInto("siteattachment")
                 .value("siteid", command.getSiteId())
                 .value("blobid", command.getBlobId())
                 .value("filename", command.getFileName())
                 .value("uploadedby", context.getUser().getUserId())
                 .value("blobsize", command.getBlobSize())
                 .value("contenttype", command.getContentType())
                 .execute(context.getTransaction());

        callback.onSuccess(new VoidResult());
    }

}
