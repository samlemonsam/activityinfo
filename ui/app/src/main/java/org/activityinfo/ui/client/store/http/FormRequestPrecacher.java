package org.activityinfo.ui.client.store.http;

import org.activityinfo.model.form.FormMetadata;

@FunctionalInterface
public interface FormRequestPrecacher {

    void precache(FormMetadata formMetadata);
}
