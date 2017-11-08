package org.activityinfo.storage;

import com.google.common.base.Optional;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public final class LocalStorageImpl implements LocalStorage {

    private static final Logger LOGGER = Logger.getLogger(LocalStorageImpl.class.getName());

    public LocalStorageImpl() {
    }

    @Override
    public void setObject(String keyName, JsonObject jsonValue) {
        setItem(keyName, jsonValue.toJson());
    }

    public native void setItem(String keyName, String json) /*-{
        $wnd.localStorage.setItem(keyName, json);
    }-*/;

    @Nullable
    @Override
    public Optional<JsonValue> getObjectIfPresent(String keyName) {
        String jsonString = getItem(keyName);
        if(jsonString != null) {
            try {
                return Optional.of(Json.parse(jsonString));
            } catch (Exception e) {
                LOGGER.warning("Exception parsing key " + keyName);
            }
        }

        return Optional.absent();
    }

    public native String getItem(String keyName) /*-{
        return $wnd.localStorage.getItem(keyName);
    }-*/;
}
