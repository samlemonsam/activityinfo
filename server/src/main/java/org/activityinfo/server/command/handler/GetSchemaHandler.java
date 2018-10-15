package org.activityinfo.server.command.handler;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.legacy.shared.impl.GetSchemaHandlerAsync;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.server.endpoint.rest.BillingAccountOracle;

/**
 * Wraps the offline version of GetSchemaHandlerAsync to add billing information
 */
public class GetSchemaHandler implements CommandHandlerAsync<GetSchema, SchemaDTO> {

    private final BillingAccountOracle oracle;

    @Inject
    public GetSchemaHandler(BillingAccountOracle oracle) {
        this.oracle = oracle;
    }

    @Override
    public void execute(GetSchema command, ExecutionContext context, AsyncCallback<SchemaDTO> outerCallback) {
        GetSchemaHandlerAsync delegate = new GetSchemaHandlerAsync();
        delegate.execute(command, context, new AsyncCallback<SchemaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                outerCallback.onFailure(caught);
            }

            @Override
            public void onSuccess(SchemaDTO result) {
                for (UserDatabaseDTO database : result.getDatabases()) {
                    database.setSuspended(oracle.isDatabaseSuspended(database.getId()));
                }
                outerCallback.onSuccess(result);
            }
        });
    }
}
