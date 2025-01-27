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
package org.activityinfo.ui.client.local.command;

import com.bedatadriven.rebar.async.AsyncFunction;
import com.bedatadriven.rebar.sql.client.*;
import com.bedatadriven.rebar.sql.client.fn.AsyncSql;
import com.bedatadriven.rebar.sql.client.fn.TxAsyncFunction;
import com.bedatadriven.rebar.sql.client.query.SqlInsert;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.util.JsonUtil;
import org.activityinfo.model.type.time.Month;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.local.sync.SyncRequestEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.activityinfo.legacy.shared.command.UpdateMonthlyReports.Change;

/**
 * Manages a persistent queue of commands to be sent to the server.
 */
@Singleton
public class CommandQueue {

    private static Logger LOGGER = Logger.getLogger(CommandQueue.class.getName());

    public static class QueueEntry {
        private int id;
        private Command command;

        public QueueEntry(int id, Command command) {
            super();
            this.id = id;
            this.command = command;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Command getCommand() {
            return command;
        }

        public void setCommand(Command command) {
            this.command = command;
        }
    }

    private SqlQuery queryNext = SqlQuery.select("id", "command").from("command_queue").orderBy("id");

    private SqlQuery queryCount = SqlQuery.select().appendColumn("count(*)", "count").from("command_queue");

    private Function<SqlResultSetRowList, Void> fireCount = new Function<SqlResultSetRowList, Void>() {

        @Override
        public Void apply(SqlResultSetRowList rows) {
            eventBus.fireEvent(new CommandQueueEvent(rows.get(0).getInt("count")));
            return null;
        }
    };

    private Function<Void, Void> fireSyncRequest = new Function<Void, Void>() {

        @Override
        public Void apply(Void argument) {
            eventBus.fireEvent(SyncRequestEvent.INSTANCE);
            return null;
        }
    };

    private Function<SqlResultSetRow, CommandQueue.QueueEntry> createEntry = new Function<SqlResultSetRow,
            CommandQueue.QueueEntry>() {

        @Override
        public QueueEntry apply(SqlResultSetRow row) {
            int id = row.getInt("id");
            String json = row.getString("command");
            return new QueueEntry(id, deserializeCommand(json));
        }
    };

    private TxAsyncFunction<CommandQueue.QueueEntry, Void> removeItem = new TxAsyncFunction<CommandQueue.QueueEntry,
            Void>() {

        @Override
        protected void doApply(SqlTransaction tx, QueueEntry argument, final AsyncCallback<Void> callback) {
            tx.executeSql("DELETE FROM command_queue WHERE id = ?",
                    new Object[]{argument.getId()},
                    new SqlResultCallback() {

                        @Override
                        public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                            callback.onSuccess(null);
                        }
                    });
        }
    };

    private final EventBus eventBus;
    private final SqlDatabase database;

    @Inject
    public CommandQueue(EventBus eventBus, SqlDatabase database) {
        this.eventBus = eventBus;
        this.database = database;
    }

    public static TxAsyncFunction<Void, Void> createTableIfNotExists() {
        return AsyncSql.ddl(
                "CREATE TABLE IF NOT EXISTS command_queue (id INTEGER PRIMARY KEY AUTOINCREMENT, command TEXT)");
    }

    /**
     * Adds a command to the queue to be executed
     *
     * @param cmd
     * @throws SQLException
     */
    public void queue(SqlTransaction tx, Command cmd) {
        JsonObject root = serialize(cmd);
        SqlInsert.insertInto("command_queue")
                 .value("command", root.toString())
                 .compose(queryCount.asFunction())
                 .compose(fireCount)
                 .compose(fireSyncRequest)
                 .apply(tx, null);
    }

    public AsyncFunction<Void, List<QueueEntry>> get() {
        return database.asFunction(queryNext.asFunction().mapSequentially(createEntry));
    }

    /**
     * Peeks at the command next line in for execution, without removing it from
     * the queue.
     *
     * @return the Command next in line for execution
     */
    public void peek(final AsyncCallback<QueueEntry> callback) {
        get().compose(new Function<List<QueueEntry>, QueueEntry>() {

            @Override
            public QueueEntry apply(List<QueueEntry> input) {
                if (input.isEmpty()) {
                    return null;
                } else {
                    return input.get(0);
                }
            }

        }).apply(null, callback);
    }

    public AsyncFunction<QueueEntry, Void> remove() {
        return database.asFunction(removeItem.compose(queryCount.asFunction()).compose(fireCount));
    }

    public void remove(QueueEntry entry, AsyncCallback<Void> callback) {
        remove().apply(entry, callback);
    }

    private JsonObject serialize(Command cmd) {
        if (cmd instanceof CreateSite) {
            return serialize((CreateSite) cmd);
        } else if (cmd instanceof UpdateSite) {
            return serialize((UpdateSite) cmd);
        } else if (cmd instanceof CreateLocation) {
            return serialize((CreateLocation) cmd);
        } else if (cmd instanceof DeleteSite) {
            return serialize((DeleteSite) cmd);
        } else if (cmd instanceof UpdateMonthlyReports) {
            return serialize((UpdateMonthlyReports)cmd);
        } else {
            throw new IllegalArgumentException("Cannot serialize commands of type " + cmd.getClass());
        }
    }

    private JsonObject serialize(DeleteSite cmd) {
        JsonObject root = new JsonObject();
        root.addProperty("commandClass", "DeleteSite");
        root.addProperty("id", cmd.getId());
        return root;
    }

    private JsonObject serialize(CreateSite cmd) {
        JsonObject root = new JsonObject();
        root.addProperty("commandClass", "CreateSite");
        root.add("properties", JsonUtil.encodeMap(cmd.getProperties().getTransientMap()));
        return root;
    }

    private JsonObject serialize(UpdateSite cmd) {
        JsonObject root = new JsonObject();
        root.addProperty("commandClass", "UpdateSite");
        root.addProperty("siteId", cmd.getSiteId());
        root.add("changes", JsonUtil.encodeMap(cmd.getChanges().getTransientMap()));
        return root;
    }

    private JsonObject serialize(CreateLocation cmd) {
        JsonObject root = new JsonObject();
        root.addProperty("commandClass", "CreateLocation");
        root.add("properties", JsonUtil.encodeMap(cmd.getProperties().getTransientMap()));
        return root;
    }

    private JsonObject serialize(UpdateMonthlyReports cmd) {
        JsonArray changeArray = new JsonArray();
        for (Change change : cmd.getChanges()) {
            JsonObject changeObject = new JsonObject();
            changeObject.addProperty("year", change.getMonth().getYear());
            changeObject.addProperty("month", change.getMonth().getMonth());
            changeObject.addProperty("indicatorId", change.getIndicatorId());
            changeObject.addProperty("value", change.getValue());
            changeArray.add(changeObject);
        }

        JsonObject root = new JsonObject();
        root.addProperty("commandClass", "UpdateMonthlyReports");
        root.addProperty("siteId", cmd.getSiteId());
        root.add("changes", changeArray);
        return root;
    }

    private Command deserializeCommand(String json) {
        JsonObject root = JsonUtil.parse(json);
        String commandClass = root.get("commandClass").getAsString();

        if ("CreateSite".equals(commandClass)) {
            return deserializeCreateSite(root);
        } else if ("UpdateSite".equals(commandClass)) {
            return deserializeUpdateSite(root);
        } else if ("CreateLocation".equals(commandClass)) {
            return deserializeCreateLocation(root);
        } else if ("DeleteSite".equals(commandClass)) {
            return deserializeDeleteSite(root);
        } else if ("UpdateMonthlyReports".equals(commandClass)) {
            return deserializeMonthlyReports(root);
        } else {
            throw new RuntimeException("Cannot deserialize queud command of class " + commandClass);
        }
    }

    private Command deserializeMonthlyReports(JsonObject root) {
        int siteId = root.get("siteId").getAsInt();
        JsonArray changeArray = root.get("changes").getAsJsonArray();
        ArrayList<Change> changes = Lists.newArrayList();
        for(int i=0;i!=changeArray.size();++i) {
            JsonObject changeObject = changeArray.get(i).getAsJsonObject();
            int indicatorId = changeObject.get("indicatorId").getAsInt();
            Month month = new Month(changeObject.get("year").getAsInt(), changeObject.get("month").getAsInt());
            Double value = null;
            if(changeObject.get("value").isJsonPrimitive()) {
                value = changeObject.get("value").getAsDouble();
            }
            changes.add(new Change(indicatorId, month, value));
        }
        return new UpdateMonthlyReports(siteId, changes);

    }

    private DeleteSite deserializeDeleteSite(JsonObject root) {
        DeleteSite cmd = new DeleteSite();
        cmd.setId(root.get("id").getAsInt());
        return cmd;
    }

    private CreateSite deserializeCreateSite(JsonObject root) {
        return new CreateSite(JsonUtil.decodeMap(root.get("properties").getAsJsonObject()));
    }

    private UpdateSite deserializeUpdateSite(JsonObject root) {
        return new UpdateSite(root.get("siteId").getAsInt(), JsonUtil.decodeMap(root.get("changes").getAsJsonObject()));
    }

    private CreateLocation deserializeCreateLocation(JsonObject root) {
        return new CreateLocation(JsonUtil.decodeMap(root.get("properties").getAsJsonObject()));
    }
}
