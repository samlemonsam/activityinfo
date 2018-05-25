package org.activityinfo.store.hrd;

import com.google.apphosting.api.ApiProxy;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.hrd.entity.FormRecordSnapshotEntity;

class SyncSizeEstimator {

    private static final long BUFFER_MS = 5_000;
    private static final int MAX_RESPONSE_SIZE = 200_000;

    private long estimatedSizeInBytes;


    public boolean timeAndSpaceRemaining() {

        // Do we have enough time left in this request to do any more work?
        long timeRemaining = ApiProxy.getCurrentEnvironment().getRemainingMillis();
        if(timeRemaining < BUFFER_MS) {
            return false;
        }

        if(estimatedSizeInBytes > MAX_RESPONSE_SIZE) {
            return false;
        }
        return true;
    }

    public void deleteRecord(String recordId) {
        estimatedSizeInBytes += recordId.length();
    }

    public void minus(FormRecordSnapshotEntity snapshot) {
        estimatedSizeInBytes -= estimateSizeInBytes(snapshot.getRecord());
    }

    public long getEstimatedSizeInBytes() {
        return estimatedSizeInBytes;
    }

    public void plus(FormRecordSnapshotEntity snapshot) {
        estimatedSizeInBytes += estimateSizeInBytes(snapshot.getRecord());
    }

    public void plus(FormRecordEntity record) {
        estimatedSizeInBytes += estimateSizeInBytes(record);
    }

    private long estimateSizeInBytes(FormRecordEntity entity) {
        int numFields = entity.getFieldValues().getProperties().size();
        return 200L + (numFields * 20L);
    }

}
