package org.activityinfo.store.hrd.columns;

import com.google.appengine.api.datastore.Blob;

public abstract class RecordIndexSet {


    public abstract RecordIndexSet add(int recordIndex);

    public abstract Blob toBlob();

    public static RecordIndexSet read(Blob blob) {
        if(blob == null) {
            return new Empty();
        } else {
            throw new UnsupportedOperationException("TODO");
        }
    }

    private static class Empty extends RecordIndexSet {

        @Override
        public RecordIndexSet add(int recordIndex) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public Blob toBlob() {
            return null;
        }
    }

}
