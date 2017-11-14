package org.activityinfo.storage;

import com.google.common.base.Optional;
import org.activityinfo.json.JsonValue;

/**
 * Provides only in-memory storage of values
 */
public class LocalStorageStub implements LocalStorage {
    @Override
    public void setObject(String keyName, JsonValue jsonValue) {
    }

    @Override
    public Optional<JsonValue> getObjectIfPresent(String keyName) {
        return Optional.absent();
    }
}
