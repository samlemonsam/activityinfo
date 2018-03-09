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
package org.activityinfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestOutput {

    public static File getDir(Class<?> testClass) {
        String relPath = testClass.getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../test-results/" + testClass.getName());
        try {
            targetDir = targetDir.getCanonicalFile();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        ensureDirExists(targetDir);
        return targetDir;
    }

    public static File getFile(Class<?> testClass, String prefix, String suffix) {
        File outputDir = getDir(testClass);
        try {
            return File.createTempFile("output", suffix, outputDir);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static File getFile(Class<?> testClass, String suffix) {
        return getFile(testClass, "output", suffix);
    }

    public static FileOutputStream open(Class<?> testClass, String fileSuffix) throws FileNotFoundException {
        return open(testClass, "output", fileSuffix);
    }

    public static FileOutputStream open(Class<?> testClass, String filePrefix, String fileSuffix) throws FileNotFoundException {
        return new FileOutputStream(getFile(testClass, fileSuffix));
    }

    public static File getBaseDir() {
        File dir = new File("build/test-output");
        ensureDirExists(dir);
        return dir;
    }

    private static void ensureDirExists(File dir) {
        if (!dir.exists()) {
            boolean succeeded = dir.mkdirs();
            if (!succeeded) {
                throw new AssertionError("Could not create directory '" + dir.getAbsolutePath() + "'");
            }
        }
    }

    public static String getBasePath() {
        return getBaseDir().getAbsolutePath();
    }
}
