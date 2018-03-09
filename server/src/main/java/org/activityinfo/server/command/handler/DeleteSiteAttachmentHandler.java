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
package org.activityinfo.server.command.handler;

import com.bedatadriven.rebar.sql.client.query.SqlUpdate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.DeleteSiteAttachment;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.legacy.shared.impl.Tables;
import org.activityinfo.server.attachment.AttachmentService;

public class DeleteSiteAttachmentHandler implements CommandHandlerAsync<DeleteSiteAttachment, VoidResult> {

    private AttachmentService attachmentService;

    @Inject
    public DeleteSiteAttachmentHandler(AttachmentService attachmentService) {
        super();
        this.attachmentService = attachmentService;
    }

    @Override
    public void execute(DeleteSiteAttachment command, ExecutionContext context, AsyncCallback<VoidResult> callback) {

        attachmentService.delete(command.getBlobId());

        SqlUpdate.delete(Tables.SITE_ATTACHMENT).where("blobid", command.getBlobId()).execute(context.getTransaction());

        callback.onSuccess(new VoidResult());
    }

}
