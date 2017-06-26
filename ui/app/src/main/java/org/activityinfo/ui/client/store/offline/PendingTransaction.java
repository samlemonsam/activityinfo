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
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Global")
public class PendingTransaction {

    private String id;
    private Date time;
    private RecordTransaction transaction;
    private RecordUpdate[] rollbacks;

    public PendingTransaction() {
    }

    public static PendingTransaction create(RecordTransaction transaction, List<RecordUpdate> rollbacks) {
        PendingTransaction p = new PendingTransaction();
        p.id = transaction.getId();
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
    public RecordUpdate[] getRollbacks() {
        return rollbacks;
    }
}
