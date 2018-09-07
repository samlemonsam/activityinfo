package org.activityinfo.ui.client.offline;

import com.bedatadriven.rebar.sql.client.SqlDatabase;
import com.bedatadriven.rebar.sql.client.SqlDatabaseFactory;
import com.google.gwt.core.client.GWT;
import org.activityinfo.legacy.shared.AuthenticatedUser;

public class DatabaseFactory {

    public static SqlDatabase get(AuthenticatedUser user) {
        SqlDatabaseFactory factory = GWT.create(SqlDatabaseFactory.class);
        return factory.open("user" + user.getUserId());
    }
}
