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
package org.activityinfo.ui.client.store.offline;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.activityinfo.model.resource.RecordTransaction;
import org.activityinfo.model.resource.RecordUpdate;

import java.util.Date;
import java.util.List;

/**
 * Stores a pending transaction, along with records neccessary to
 * roll it back locally
 *
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final class PendingTransaction {

    /**
     * The update is ready to send to the server
     */
    @JsOverlay
    public static final String READY = "ready";

    /**
     * The update has been submitted to the server and we are waiting for a response.
     */
    @JsOverlay
    public static final String PENDING = "pending";

    /**
     * The update was rejected by the server.
     */
    @JsOverlay
    public static final String FAILED = "failed";

    private String id;
    private Date time;
    private RecordTransaction transaction;
    private RecordUpdate[] rollbacks;
    private String status;

    /**
     * The time (in milliseconds) at which this transaction was checked out of the queue
     * and set to "PENDING"
     */
    private double checkoutTime;

    public PendingTransaction() {
    }

    @JsOverlay
    public static PendingTransaction create(RecordTransaction transaction, List<RecordUpdate> rollbacks) {
        PendingTransaction p = new PendingTransaction();
        p.id = transaction.getId();
        p.status = READY;
        p.time = new Date();
        p.transaction = transaction;
        p.rollbacks = rollbacks.toArray(new RecordUpdate[rollbacks.size()]);
        return p;
    }

    @JsOverlay
    public String getId() {
        return id;
    }

    @JsOverlay
    public Date getTime() {
        return time;
    }

    @JsOverlay
    public RecordTransaction getTransaction() {
        return transaction;
    }

    @JsOverlay
    public String getStatus() {
        return status;
    }

    @JsOverlay
    public boolean isReady() {
        return READY.equals(getStatus());
    }

    @JsOverlay
    public RecordUpdate[] getRollbacks() {
        return rollbacks;
    }

    @JsOverlay
    public void setStatus(String status) {
        this.status = status;
    }

    @JsOverlay
    public double getCheckoutTime() {
        return checkoutTime;
    }

    @JsOverlay
    public void setCheckoutTime(double checkoutTime) {
        this.checkoutTime = checkoutTime;
    }
}
