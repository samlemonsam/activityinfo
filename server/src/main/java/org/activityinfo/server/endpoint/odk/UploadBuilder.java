package org.activityinfo.server.endpoint.odk;

import org.activityinfo.io.xform.form.BindingType;
import org.activityinfo.io.xform.form.Upload;

class UploadBuilder implements OdkFormFieldBuilder {
    final private String mediaType;

    UploadBuilder(String mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public BindingType getModelBindType() {
        return BindingType.BINARY;
    }

    @Override
    public Upload createBodyElement(String ref, String label, String hint) {
        Upload upload = new Upload();

        upload.setRef(ref);
        upload.setMediaType(mediaType);
        upload.setLabel(label);
        upload.setHint(hint);

        return upload;
    }
}
