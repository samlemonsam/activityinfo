package org.activityinfo.storage;

import com.google.common.base.Optional;
import com.google.gwt.storage.client.Storage;
import org.activityinfo.json.JsonValue;

/**
 * Provides local, in-browser storage of user preferences.
 */
public interface LocalStorage {

    void setObject(String keyName, JsonValue jsonValue);

    Optional<JsonValue> getObjectIfPresent(String keyName);

    static LocalStorage create() {
        if(Storage.isLocalStorageSupported()) {
            return new LocalStorageImpl();
        } else {
            return new LocalStorageStub();
        }
    }
}
