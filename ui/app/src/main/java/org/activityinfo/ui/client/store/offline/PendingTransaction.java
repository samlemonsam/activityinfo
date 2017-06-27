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
}
