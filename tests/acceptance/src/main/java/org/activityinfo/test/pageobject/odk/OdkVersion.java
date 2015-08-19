package org.activityinfo.test.pageobject.odk;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;

public class OdkVersion {

    public static final String LATEST = "1.4_rev_1038";
    private String version;

    public OdkVersion(String version) {
        this.version = version;
    }

    public String getApkLocalPath() {
        return new File(getApkResource().getFile()).getAbsolutePath();
    }

    public String getApkName() {
        return "odk_collect_" + version + ".apk";
    }

    public ByteSource getApk() {
        return Resources.asByteSource(getApkResource());
    }

    private URL getApkResource() {
        return Resources.getResource("org/activityinfo/test/odk/" + getApkName());
    }

    public String toString() {
        return version;
    }
    
    public static OdkVersion latest() {
        return new OdkVersion(LATEST);
    }

    public static List<OdkVersion> selectedVersions() throws IOException {

        String version = System.getProperty("odkVersion", LATEST);
        if(version.equals("ALL")) {
            List<OdkVersion> versions = Lists.newArrayList();
            for(String line : readLines(getResource("odkVersions"), Charsets.UTF_8)) {
                versions.add(new OdkVersion(line));
            }
            return versions;

        } else {
            return Collections.singletonList(new OdkVersion(version));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OdkVersion that = (OdkVersion) o;

        if (!version.equals(that.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }
}
