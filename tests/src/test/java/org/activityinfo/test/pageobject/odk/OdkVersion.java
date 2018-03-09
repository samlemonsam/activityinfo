/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
