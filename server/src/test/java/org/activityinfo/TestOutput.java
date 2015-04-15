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
