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
package org.activityinfo.server.endpoint.gwtrpc;

import com.bedatadriven.rebar.sql.client.SqlException;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.SqlTransactionCallback;
import com.bedatadriven.rebar.sql.server.jdbc.JdbcScheduler;
import com.bedatadriven.rebar.sql.shared.adapter.SyncTransactionAdapter;
import com.google.cloud.trace.core.TraceContext;
import com.google.common.base.Stopwatch;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Injector;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.legacy.shared.impl.CommandHandlerAsync;
import org.activityinfo.legacy.shared.impl.ExecutionContext;
import org.activityinfo.server.command.handler.CommandHandler;
import org.activityinfo.server.command.handler.HandlerUtil;
import org.activityinfo.server.database.hibernate.HibernateExecutor;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.Trace;
import org.hibernate.ejb.HibernateEntityManager;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteExecutionContext implements ExecutionContext {

    private static final Logger LOGGER = Logger.getLogger(RemoteExecutionContext.class.getName());

    private static final ThreadLocal<RemoteExecutionContext> CURRENT = new ThreadLocal<RemoteExecutionContext>();

    private AuthenticatedUser user;
    private Injector injector;
    private SyncTransactionAdapter tx;
    private HibernateEntityManager entityManager;
    private JdbcScheduler scheduler;

    public RemoteExecutionContext(Injector injector) {
        super();
        this.injector = injector;
        this.user = injector.getInstance(AuthenticatedUser.class);
        this.entityManager = (HibernateEntityManager) injector.getInstance(EntityManager.class);
        this.scheduler = new JdbcScheduler();
        this.scheduler.allowNestedProcessing();
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public AuthenticatedUser getUser() {
        return user;
    }

    @Override
    public SqlTransaction getTransaction() {
        return tx;
    }

    public static RemoteExecutionContext current() {
        RemoteExecutionContext current = CURRENT.get();
        if (current == null) {
            throw new IllegalStateException("No current command execution context");
        }
        return current;
    }

    public static boolean inProgress() {
        return CURRENT.get() != null;
    }

    /**
     * Executes the top-level command, starting a database transaction
     */
    public <C extends Command<R>, R extends CommandResult> R startExecute(final C command) {

        
        if (CURRENT.get() != null) {
            throw new IllegalStateException("Command execution context already in progress");
        }

        try {
            CURRENT.set(this);
            /*
             * Begin the transaction
             */
            this.entityManager.getTransaction().begin();

            /*
             * Setup an async transaction simply wrapping the hibernate
             * transaction
             */
            this.tx = new SyncTransactionAdapter(new HibernateExecutor(this.entityManager),
                    scheduler,
                    new TransactionCallback());
            this.tx.withManualCommitting();

            /*
             * Execute the command
             */

            R result;

            try {
                result = execute(command);

                scheduler.process();

            } catch (Exception e) {
                /*
                 * If the execution fails, rollback
                 */
                try {
                    this.entityManager.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    LOGGER.log(Level.SEVERE, "Exception rolling back failed transaction", rollbackException);
                }

                /*
                 * Rethrow exception, wrapping if necessary
                 */
                throw wrapException(e);
            }

            /*
             * Commit the transaction
             */

            LOGGER.info("Committing transaction for " + command);
            
            try {
                this.entityManager.flush();
                this.entityManager.getTransaction().commit();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Commit failed!", e);
                throw new RuntimeException("Commit failed", e);
            }

            LOGGER.info("Transaction committed");


            return result;

        } finally {
            CURRENT.remove();
        }
    }

    /**
     * Executes a (nested) command synchronously. This is called from within
     * CommandHandlers to execute nested commands
     */
    public <C extends Command<R>, R extends CommandResult> R execute(final C command) {

        if (tx == null) {
            throw new IllegalStateException("Command execution has not started yet");
        }

        ResultCollector<R> collector = new ResultCollector<>(command.getClass());
        execute(command, collector);

        scheduler.process();

        return collector.get();
    }

    /**
     * Executes a (nested) command (pseudo) asynchronously. This is called from
     * within CommandHandlers to execute nested commands.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R extends CommandResult> void execute(final C command,
                                                                        final AsyncCallback<R> outerCallback) {

        LOGGER.info("Executing " + command.getClass().getSimpleName() + " for " + user.getEmail());

        AsyncCallback<R> callback = new FiringCallback<>(command, outerCallback);


        Object handler = injector.getInstance(HandlerUtil.handlerForCommand(command));

        if (handler instanceof CommandHandlerAsync) {
            /*
             * Execute Asynchronously
             */
            ((CommandHandlerAsync<C, R>) handler).execute(command, this, callback);

        } else if (handler instanceof CommandHandler) {
            /*
             * Executes Synchronously
             */
            try {
                callback.onSuccess((R) ((CommandHandler) handler).execute(command, retrieveUserEntity()));
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }
    }

    public User retrieveUserEntity() {
        return entityManager.find(User.class, user.getId());
    }

    private static RuntimeException wrapException(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        } else {
            LOGGER.log(Level.SEVERE, "Unexpected command exception: " + t.getMessage(), t);
            return new RuntimeException(t);
        }
    }

    private static class TransactionCallback extends SqlTransactionCallback {

        @Override
        public void begin(SqlTransaction tx) {
            // we actually start the transaction our self, so we know it
            // is already active.
        }

        @Override
        public void onError(SqlException e) {
            throw e;
        }
    }

    private class FiringCallback<R extends CommandResult> implements AsyncCallback<R> {
        private final AsyncCallback<R> callback;
        private final String commandName;
        private final Optional<TraceContext> traceContext;
        private final Stopwatch stopwatch = Stopwatch.createStarted();

        public FiringCallback(Command command, AsyncCallback<R> callback) {
            super();
            this.callback = callback;
            commandName = command.getClass().getSimpleName();
            traceContext = Trace.startSpan("ai/cmd/" + command.getClass().getSimpleName());
        }

        @Override
        public void onFailure(Throwable caught) {
            Trace.endSpan(traceContext);
            LOGGER.log(Level.SEVERE, commandName + " failed with exception " + caught.getClass().getSimpleName(), caught);
            callback.onFailure(caught);
        }

        @Override
        public void onSuccess(final R result) {
            Trace.endSpan(traceContext);
            LOGGER.info(commandName + " finished in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
            callback.onSuccess(result);
        }
    }

    private class ResultCollector<R> implements AsyncCallback<R> {

        private Class<? extends Command> commandClass;
        private int callbackCount = 0;
        private R result = null;
        private Throwable caught = null;

        public ResultCollector(Class<? extends Command> commandClass) {
            super();
            this.commandClass = commandClass;
        }

        @Override
        public void onFailure(Throwable caught) {
            this.callbackCount++;
            if (callbackCount > 1) {
                throw new RuntimeException("Callback for '" + commandClass + "' called multiple times");
            }
            this.caught = caught;
        }

        public R get() throws CommandException {
            if (callbackCount != 1) {
                throw new IllegalStateException("Callback for '" + commandClass + "' called " + callbackCount + " times");
            } else if (caught != null) {
                throw wrapException(caught);
            }
            return result;
        }

        @Override
        public void onSuccess(R result) {
            callbackCount++;
            if (callbackCount > 1) {
                throw new RuntimeException("Callback called multiple times");
            }
            this.result = result;
        }
    }
}