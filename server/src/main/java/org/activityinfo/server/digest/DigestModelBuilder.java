package org.activityinfo.server.digest;

import java.io.IOException;

public interface DigestModelBuilder {

    public abstract DigestModel createModel(UserDigest userDigest) throws IOException;

}