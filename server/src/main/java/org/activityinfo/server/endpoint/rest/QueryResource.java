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
package org.activityinfo.server.endpoint.rest;

import com.google.common.base.Charsets;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.store.query.output.ColumnJsonWriter;
import org.activityinfo.store.query.output.RowBasedJsonWriter;
import org.activityinfo.store.query.server.ColumnSetBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;


public class QueryResource {

    private final ApiBackend backend;

    public QueryResource(ApiBackend backend) {
        this.backend = backend;
    }

    @POST
    @Path("columns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryColumns(QueryModel model) {

        ColumnSetBuilder builder = backend.newQueryBuilder();

        final ColumnSet columnSet = builder.build(model);

        final StreamingOutput output = outputStream -> {
            ColumnJsonWriter columnSetWriter = new ColumnJsonWriter(outputStream, Charsets.UTF_8);
            columnSetWriter. write(columnSet);
            columnSetWriter.flush();
        };

        return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("rows")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryRows(QueryModel model) {

        ColumnSetBuilder builder = backend.newQueryBuilder();

        final ColumnSet columnSet = builder.build(model);

        final StreamingOutput output = outputStream -> {
            RowBasedJsonWriter writer = new RowBasedJsonWriter(outputStream, Charsets.UTF_8);
            writer. write(columnSet);
            writer.flush();
        };

        return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

}
